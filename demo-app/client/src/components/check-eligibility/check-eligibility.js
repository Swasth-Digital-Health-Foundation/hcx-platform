import React, { useEffect, useState, useRef } from 'react';
import { Row, Container, Button, Form, Col, Alert } from 'react-bootstrap'
import './check-eligibility.css';
import { useMachine } from '@xstate/react'
import { machine } from '../../machines/checkEligibility';
import Info from '../info/info';
import Spinner from '../spinner/spinner';
import { io } from 'socket.io-client';
import scenarios from './scenarios.json';
import { get as _get, find as _find } from 'lodash-es'
import Table from '../table/table';
import { useHistory } from "react-router-dom";

const socket = io();

const EligibilityCheck = props => {
    const [current, send] = useMachine(machine);
    const [validated, setValidated] = useState(false);
    const history = useHistory();

    const handleSubmit = (event) => {
        event.preventDefault();
        const form = event.currentTarget;

        if (form.checkValidity() === false) {
            event.stopPropagation();
            setValidated(true);
            return;
        }

        const { policyId, scenario, name, gender, amount } = form.elements;
        const index = parseInt(scenario.value);
        const scenarioObject = _find(scenarios, { index });
        const requestType = event.nativeEvent.submitter.dataset.type;
        const payload = {
            requestType,
            payload: {
                policyId: policyId.value,
                name: `${name.value}`,
                gender: gender.value,
                ...(scenarioObject && {
                    ...scenarioObject.body
                }),
                ...(amount && {
                    amount: amount.value
                })
            }
        };

        if (['CLAIM', 'PREAUTH'].includes(requestType)) {
            send('RETRY');
        }

        send('SUBMIT', payload);
    };

    const formatErrorResponse = (payload) => {
        const { responseTime = 0, error = {} } = payload;
        return {
            error: error?.message,
            responseTime: `${responseTime / 1000} seconds`
        }
    };

    const formatSuccessResponse = (payload) => {
        const { responseTime = 0, entry = [] } = payload;
        const errorDetails = payload['x-hcx-error_details'];
        const coverageEligibilityResponse = _find(entry, e => e?.resource?.resourceType === 'CoverageEligibilityResponse');
        const claimResponse = _find(entry, e => e?.resource?.resourceType === 'ClaimResponse');
        const preAuthResponse = _find(entry, e => e?.resource?.resourceType === 'PreauthResponse');

        const policyIdElement = document.getElementById('policyId');
        const nameElement = document.getElementById('patientName');
        const genderElement = document.getElementById('gender');

        return {
            ...(!errorDetails && {
                name: nameElement?.value,
                gender: genderElement?.value,
                policyId: policyIdElement?.value,
                ...(coverageEligibilityResponse && {
                    status: _get(coverageEligibilityResponse, 'resource.status'),
                    policyStart: _get(coverageEligibilityResponse, 'resource.servicedPeriod.start'),
                    policyEnd: _get(coverageEligibilityResponse, 'resource.servicedPeriod.end'),
                }),
                ...(claimResponse && {
                    amount: `${_get(claimResponse, 'resource.item[0].adjudication[0].amount.value') || 0} ${_get(claimResponse, 'resource.item[0].adjudication[0].amount.currency')} `,
                }),
                ...(preAuthResponse && {
                    amount: `${_get(preAuthResponse, 'resource.item[0].adjudication[0].amount.value') || 0} ${_get(preAuthResponse, 'resource.item[0].adjudication[0].amount.currency')} `,
                }),
            }),
            ...(errorDetails && {
                error: errorDetails?.message
            }),
            responseTime: `${responseTime / 1000} seconds`
        }
    };


    useEffect(() => {
        socket.on('acknowledgement', response => {
            console.log({ response });
            current.value === 'acknowledged' && send('ACKNOWLEDGEMENT_SUCCESS', { payload: response });
        })
    });

    return (
        <>
            <Container>
                <Row>
                    <Col className='m-3' >
                        <Form noValidate validated={validated} onSubmit={handleSubmit} id="hcx-form">
                            <Alert variant='dark'> Patient Details</Alert>

                            <Row className="mb-3">
                                <Form.Group as={Col}>
                                    <Form.Label>Patient Policy Id</Form.Label>
                                    <Form.Control name="policyId" type="text" placeholder="Policy Id" required id='policyId' />
                                    <Form.Control.Feedback type="invalid" >
                                        Please enter the Policy Id
                                    </Form.Control.Feedback>
                                </Form.Group>

                                <Form.Group as={Col}>
                                    <Form.Label>Scenarios</Form.Label>
                                    <Form.Select name='scenario' required>
                                        {
                                            scenarios.map((scenario) => {
                                                const { index, label, selected = false } = scenario;
                                                return <option value={index} selected={selected} key={index}> {label}</option>
                                            })
                                        }
                                    </Form.Select>

                                    <Form.Control.Feedback type="invalid">
                                        Please select a scenario from the list.
                                    </Form.Control.Feedback>
                                </Form.Group>
                            </Row>

                            <Row className="mb-3">
                                <Form.Group as={Col}>
                                    <Form.Label>Name</Form.Label>
                                    <Form.Control name="name" type="text" placeholder="Name" required id='patientName' />
                                    <Form.Control.Feedback type="invalid">
                                        Name is required
                                    </Form.Control.Feedback>
                                </Form.Group>

                                <Form.Group as={Col}>
                                    <Form.Label className='mb-3'>Gender</Form.Label>
                                    <div className="mb-3">
                                        <Form.Check
                                            inline
                                            id='gender'
                                            label="Male"
                                            name="gender"
                                            type="radio"
                                            value="Male"
                                            required
                                        />
                                        <Form.Check
                                            inline
                                            id='gender'
                                            label="Female"
                                            name="gender"
                                            type="radio"
                                            value="Female"
                                            required
                                        />
                                    </div>
                                </Form.Group>

                                {current.value === 'resolved' && !current.context?.hcxResponse['x-hcx-error_details'] && <Form.Group as={Col}>
                                    <Form.Label>Amount</Form.Label>
                                    <Form.Control name="amount" type="number" placeholder="Amount" required id='amount' />
                                    <Form.Control.Feedback type="invalid" >
                                        Please enter the Amount
                                    </Form.Control.Feedback>
                                </Form.Group>}

                            </Row>

                            {current.value === 'initial' && <Button variant="primary" type="submit" className='m-2' data-type="ELIGIBILITY" form='hcx-form' value="CLAIM"> Check eligibility </Button>}
                            {current.value === 'resolved' && !current.context?.hcxResponse['x-hcx-error_details'] && <Button variant="primary" type="submit" className='m-2' data-type="CLAIM" form="hcx-form" value="CLAIM"> Claim </Button>}
                            {current.value === 'resolved' && !current.context?.hcxResponse['x-hcx-error_details'] && <Button variant="primary" type="submit" className='m-2' data-type="PREAUTH" form="hcx-form" value="PREAUTH"> Pre Auth </Button>}

                            {['resolved', 'rejected'].includes(current.value) && <>
                                <Button variant="primary" type="button" onClick={e => send('RETRY')} className="m-2"> RETRY </Button>
                                <Button variant="primary" type="button" className="m-2" onClick={e => history.push({ pathname: '/json-viewer', state: current.context.hcxResponse || current.context.error })}> Complete Response  </Button>
                                {current.context?.request && <Button variant="primary" type="button" className="m-2" onClick={e => history.push({ pathname: '/json-viewer', state: current.context.request })}> Complete Request  </Button>}
                            </>}

                            {current.value === 'loading' && <Spinner message={current.context.message} />}
                            {current.value === 'acknowledged' && <Spinner message={current.context.message} />}

                        </Form>
                    </Col>
                </Row>
                <Row>
                    <Col className='m-3' >
                        {current.value === 'rejected' && <Info type='danger' heading="Error !!" body={current.context?.error?.error?.message || current.context.message} />}
                        {current.value === 'resolved' && <Info type='success' heading="Success !!" body={current.context.message} footer={formatSuccessResponse({ ...current?.context?.hcxResponse, responseTime: current.context.timeTakenForResponse })} />}

                        {/* show error in tabluar format */}
                        {current.value === 'rejected' && <Table data={formatErrorResponse({ ...current.context.error, responseTime: current.context.timeTakenForResponse })}></Table>}
                        {current.value === 'resolved' && <Table data={formatSuccessResponse({ ...current?.context?.hcxResponse, responseTime: current.context.timeTakenForResponse })}></Table>}
                        {console.log(current.context.event)}
                    </Col>
                </Row>
            </Container>
        </>
    );
}

export { EligibilityCheck }