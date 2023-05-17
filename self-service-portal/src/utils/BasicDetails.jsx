import React, { useState, useEffect } from 'react'
import { Button, Form, Grid, Loader, Message } from 'semantic-ui-react'
import { post } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import { useQuery } from '../service/QueryService';
import Dropdown from 'react-dropdown';
import 'react-dropdown/style.css';
import { getParticipantSearch } from '../service/RegistryService';
import * as _ from 'lodash';
import { useSelector } from 'react-redux';


export const BasicDetails = ({ changeTab, formState, setState }) => {

    const mockPayorCode = process.env.REACT_APP_MOCK_PAYOR_CODE;
    const tag = process.env.REACT_APP_PARTICIPANT_TAG;
    const env = process.env.REACT_APP_ENV;

    const { register, handleSubmit, watch, formState: { errors }, reset, setValue, getValues } = useForm();
    const [sending, setSending] = useState(false)
    const [payorList, setPayorList] = useState([])
    const [payor, setPayor] = useState({})
    const [isJWTPresent, setIsJWTPresent] = useState(false)
    const [primaryEmail, setPrimaryEmail] = useState("")
    const [primaryMobile, setPrimaryMobile] = useState("")
    const [participantName, setParticipantName] = useState("")
    const [invalidApplicantCode, setInvalidApplicantCode] = useState(false)
    const watchRoles = watch("roles", "payor")
    const watchOnboardThrough = watch("onboard_through", "")
    const watchApplicantCode = watch("applicant_code", "")
    const [applicantCode, setApplicantCode] = useState("")
    const [fetchResponse, setFetchResponse] = useState(false)
    const formStore = useSelector((state) => state)
    const query = useQuery();
    const [fields, setFields] = useState([]);
    const [formErrors, setFormErrors] = useState({});


    const getPayor = participantName => {
        const participant = payorList.find(participant => participant.participant_name === participantName);
        if (participant) {
            setPayor(participant)
        }
    }

    useEffect(() => {
        const jwtToken = query.get("jwt_token");
        setIsJWTPresent(jwtToken ? true : false);

        if (_.size(_.keys(jwtToken)) != 0) {
            getParticipantDetails({})
        }

        (async () => {
            try {
                const participants = await getParticipantSearch({}).then(response => response.data.participants || []);
                const participantNames = participants.map(participant => ({ value: participant.participant_name, ...participant }))
                setPayorList(participantNames)
            } catch (error) {
                setPayorList([])
            }
        })()

        if(env !== 'staging'){
            setValue("onboard_through", 'actual_payor');
        }
    }, []);

    const setPrimaryEmailState = email => {
        setPrimaryEmail(email);
        setFormErrors({});
    }

    const onSubmit = (data) => {
        setSending(true)
        const jwtToken = query.get("jwt_token");
        let formData;

        if (fields.length != 0) {
            fields.forEach(function (field) {
                field.value = data[field.name];
                delete field.id;
            });
        }

        if (isJWTPresent) {
            formData = [{ "type": "onboard-through-jwt", "jwt": jwtToken, additionalVerification: fields, "participant": { "participant_name": data.participant_name, "primary_email": data.primary_email, "primary_mobile": data.primary_mobile, "roles": ["provider"] } }];
        } else if (data.roles === 'payor') {
            formData = [{ "participant": { "participant_name": data.participant_name, "primary_email": data.primary_email, "primary_mobile": data.primary_mobile, "roles": [data.roles] } }];
        } else if (payor != null && !invalidApplicantCode) {
            formData = [{ "type": "onboard-through-verifier", "verifier_code": payor.participant_code, "applicant_code": applicantCode, additionalVerification: fields, "participant": { "participant_name": data.participant_name, "primary_email": data.primary_email, "primary_mobile": data.primary_mobile, "roles": ["provider"] } }];
        }

        if (data.onboard_through === 'mock_payor') {
            formData[0]['verifier_code'] = mockPayorCode;
        }

        if (tag) {
            formData[0].participant['tag'] = [tag]
        }

        post("/participant/verify", JSON.stringify(formData))
            .then((data => {
                reset();
                setState({ ...formState, ...(formData[0]), ...{ "participant_code": _.get(data, 'data.result.participant_code'), "verifier_code": payor.participant_code, "identity_verification": _.get(data, 'data.result.identity_verification') } })
                setTimeout(() => {
                    changeTab(1)
                }, 1000);
            })).catch(err => {
                if (_.get(err, 'response.data.error.message') && _.get(err, 'response.data.error.message') == "Username already invited / registered for Organisation") {
                    setFormErrors({ primary_email: 'This email address already exists' });
                } else {
                    toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                        position: toast.POSITION.TOP_CENTER
                    });
                }
            }).finally(() => {
                setTimeout(() => {
                    setSending(false)
                }, 1000);
            })
    }

    const getParticipantDetails = () => {
        setSending(true)
        let payload;

        if (watchApplicantCode && payor) {
            payload = { "applicant_code": applicantCode, "verifier_code": payor.participant_code }
        } else {
            payload = { "verification_token": query.get("jwt_token") }
        }

        const payorSystem = _.get(formStore.formState, 'payor_system');

        if (payorSystem) {
            setDetails(payorSystem.participant_name, payorSystem.primary_email, payorSystem.primary_mobile)
            setSending(false)
            setFetchResponse(true);
        } else {
            post("/applicant/getinfo", JSON.stringify(payload)).then((data => {
                let respBody = data.data;

                const additionalFields = respBody.additionalVerification || [];
                if (additionalFields.length != 0) {
                    for (let i = 0; i < additionalFields.length; i++) {
                        setFields((fields) => [...fields, { id: fields.length + 1, name: additionalFields[i].name, label: additionalFields[i].label, pattenr: additionalFields[i].pattern }])
                    }
                }

                setDetails(_.get(respBody, 'applicant_name') || "", _.get(respBody, 'email') || "", _.get(respBody, 'mobile') || "")
                setFetchResponse(true);
            })).catch((err => {
                console.error(err)
                let errMsg = _.get(err, 'response.data.error.message')
                if (typeof errMsg === 'string' && errMsg.includes('UnknownHostException')) {
                    setFormErrors({ applicant_code: 'Payor system in unavailable, Please try later!' });
                } else {
                    toast.error(errMsg || "Internal Server Error", {
                        position: toast.POSITION.TOP_CENTER
                    });
                }
                setInvalidApplicantCode(true);
                setFetchResponse(true);
            })).finally(() => {
                setSending(false)
            })
        }
    }

    function setDetails(participantName, primaryEmail, primaryMobile) {
        setPrimaryEmail(primaryEmail || "");
        setPrimaryMobile(primaryMobile || "");
        setParticipantName(participantName || "");
        setValue("primary_email", primaryEmail);
        setValue("primary_mobile", primaryMobile);
        setValue("participant_name", participantName);
    }

    function setMockDetails() {
        payor['participant_code'] = mockPayorCode;
        setApplicantCode(Math.floor(10000000 + Math.random() * 90000000).toString().substr(0, 8))
        setFetchResponse(true)
    }

    function resetMockDetails() {
        if (payor) {
            delete payor['participant_code']
        }
        setApplicantCode('')
        setFetchResponse(false)
    }

    return <>
        <ToastContainer autoClose={false} />
        <Form disabled={sending} onSubmit={handleSubmit(onSubmit)} className="container">
            {sending && <Loader active />}
            <div className='form-main' style={{ marginTop: '25px' }}>
                <Grid columns='equal'>
                    {!isJWTPresent ?
                        <Form.Field disabled={sending} className={{ 'error': 'roles' in errors }} required>
                            <label>Roles:</label>
                        </Form.Field> : null}
                    {!isJWTPresent ?
                        <Form.Field disabled={sending}>
                            <input
                                id="payor"
                                type="radio"
                                label='Payor'
                                name='roles'
                                value='payor'
                                {...register("roles", { required: true })}
                                defaultChecked
                            /> Payor
                        </Form.Field> : null}
                    {!isJWTPresent ?
                        <Form.Field disabled={sending}>
                            <input
                                id="provider"
                                type="radio"
                                label='Provider'
                                name='roles'
                                value='provider'
                                {...register("roles", { required: true })}
                            /> Provider
                        </Form.Field> : null}
                </Grid>
                { env === 'staging' ?
                <Grid columns='equal'>
                    {!isJWTPresent && watchRoles === "provider" ?
                        <Form.Field disabled={sending} className={{ 'error': 'onboard_through' in errors }} required>
                            <label>Onboard Through:</label>
                        </Form.Field> : null}
                    {!isJWTPresent && watchRoles === "provider" ?
                        <Form.Field disabled={sending}>
                            <input
                                id="mock_payor"
                                type="radio"
                                label='Mock Payor'
                                name='onboard_through'
                                value='mock_payor'
                                onClick={e => { setMockDetails() }}
                                {...register("onboard_through", { required: true })}
                            /> Mock Payor
                        </Form.Field> : null}
                    {!isJWTPresent && watchRoles === "provider" ?
                        <Form.Field disabled={sending}>
                            <input
                                id="actual_payor"
                                type="radio"
                                label='actual_payor'
                                name='onboard_through'
                                value='actual_payor'
                                onClick={e => { resetMockDetails() }}
                                {...register("onboard_through", { required: true })}
                            /> Actual Payor
                        </Form.Field> : null}
                </Grid> : null }
                {/* {watchOnboardThrough === 'mock_payor' ?
                    <Message disabled={sending} style={{ textAlign: 'left' }} visible>Please enter the below details</Message>
                    : null} */}
                {watchRoles === "provider" && watchOnboardThrough === 'actual_payor' ?
                    <Message disabled={sending} style={{ textAlign: 'left' }} visible>Select the payor and enter the <b>applicant code</b> and click on <b>fetch details</b>. Using the applicant code, details will fetched from the selected payor system and populated in the form.</Message>
                    : null}
                {!isJWTPresent && watchRoles === "provider" && watchOnboardThrough === 'actual_payor' ?
                    <Form.Field disabled={sending} style={{ marginTop: '12px' }} className={{ 'error': _.size(_.keys(errors)) > 0 && _.size(_.keys(payor)) == 0 }} required>
                        <label>Payors</label>
                        <Dropdown className='input-text' placeholder='Select Payor' fluid selection options={payorList} onChange={e => getPayor(e?.value)} required />
                    </Form.Field>
                    : null
                }
                {!isJWTPresent && watchRoles === "provider" && watchOnboardThrough === 'actual_payor' ?
                    <Grid.Row>
                        <Form.Field disabled={sending} style={{ marginBottom: '15px' }} className={{ 'error': 'applicant_code' in errors }} required>
                            <label>Applicant Code</label>
                            <input className='input-text' placeholder='Enter Applicant Code' onInput={e => setApplicantCode(e.target.value)} {...register("applicant_code", { required: true })} />
                        </Form.Field>
                        {fetchResponse ? null :
                            <Button disabled={sending} onClick={e => {
                                e.preventDefault()
                                getParticipantDetails()
                            }} className="primary center-element button-color">
                                {sending ? "Fecthing" : "Fetch Details"}</Button>
                        }
                    </Grid.Row>
                    : null
                }
                {watchRoles === "payor" || fetchResponse === true ?
                    <Form.Field disabled={sending} className={{ 'error': (primaryEmail === '' && 'primary_email' in errors) || formErrors.primary_email }} required>
                        <label>Email</label>
                        <input className='input-text' type='email' placeholder='Enter Email' value={primaryEmail} onInput={e => setPrimaryEmailState(e.target.value)} {...register("primary_email", { required: true, pattern: /^\S+@\S+$/i, message: "Email required" })} />
                        {formErrors.primary_email && (<Grid.Row centered><div style={{ color: 'red', marginTop: '5px' }}>{formErrors.primary_email}</div></Grid.Row>)}
                    </Form.Field> : null}
                {watchRoles === "payor" || fetchResponse === true ?
                    <Form.Field disabled={sending} className={{ 'error': primaryMobile === '' && 'primary_mobile' in errors }} required>
                        <label>Phone Number</label>
                        <input className='input-text' placeholder='Enter Phone Number' value={primaryMobile} onInput={e => setPrimaryMobile(e.target.value)} {...register("primary_mobile", { required: true, pattern: /^[0-9]{10}/i })} />
                    </Form.Field> : null}
                {watchRoles === "payor" || fetchResponse === true ?
                    <Form.Field disabled={sending} className={{ 'error': participantName === '' && 'participant_name' in errors }} required>
                        <label>Organisation Name</label>
                        <input className='input-text' placeholder='Enter Organisation Name' value={participantName} onInput={e => setParticipantName(e.target.value)}  {...register("participant_name", { required: true })} />
                    </Form.Field> : null}
                {fields.length !== 0 ?
                    <b>Additional Fields(Requested by payor):</b>
                    : null}
                {fields.map(field => (
                    <Form.Field disabled={sending} key={field.id}>
                        <label>{field.label}</label>
                        <input placeholder={`Enter ${field.label}`} {...register(field.name, { required: true, pattern: field.pattern })} />
                    </Form.Field>
                ))}
            </div><br />
            {watchRoles === "payor" || fetchResponse === true ?
                <Button className={{ 'disabled': sending, 'primary center-element button-color': true }} disabled={sending} type='submit'>
                    {sending ? "Submitting" : "Submit"}
                </Button> : null}
        </Form>
    </>

}