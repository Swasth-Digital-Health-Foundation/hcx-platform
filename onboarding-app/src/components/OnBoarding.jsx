import React, { useState, useEffect, useCallback } from 'react'
import { Button, Form, Segment, Grid, Image, Radio } from 'semantic-ui-react'
import { sendData } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast, autoClose } from 'react-toastify';
import { useQuery } from '../service/QueryService';
import Dropdown from 'react-dropdown';
import 'react-dropdown/style.css';
import { getParticipantSearch } from '../service/RegistryService';
import * as _ from 'lodash';
import debounce from 'lodash.debounce';


export const OnBoarding = () => {
    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();
    const [sending, setSending] = useState(false)
    const [payorList, setPayorList] = useState([])
    const [payor, setPayor] = useState({})
    const [isJWTPresent, setIsJWTPresent] = useState(false)
    const [primaryEmail, setPrimaryEmail] = useState("")
    const [primaryMobile, setPrimaryMobile] = useState("")
    const [participantName, setParticipantName] = useState("")
    const watchRoles = watch("roles", "payor")
    const watchApplicantCode = watch("applicant_code", "")

    let query = useQuery();

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
    }, []);

    const onSubmit = (data) => {
        setSending(true)
        const jwtToken = query.get("jwt_token");
        let formData;

        if (isJWTPresent) {
            formData = [{ "jwt_token": jwtToken, "participant": { ...data, "roles": [data.roles] } }];
        } else if (payor != null) {
            formData = [{ "payor_code": payor.participant_code, "applicant_code": data.applicant_code, "participant": { ...data, "roles": [data.roles] } }];
        } else {
            formData = [{ "participant": { ...data, "roles": [data.roles] } }];
        }

        sendData("/participant/verify", JSON.stringify(formData)).then((data => {
            toast.success("Form is submitted successfully", {
                position: toast.POSITION.TOP_CENTER, autoClose: 2000
            });
            reset()
        })).catch(err => {
            toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                position: toast.POSITION.TOP_CENTER
            });
        }).finally(() => {
            setSending(false)
        })
    }


    const getParticipantDetails = ({ value, payor }) => {
        let payload;
        if (value && payor) {
            payload = { "applicant_code": value.target.value, "payor_code": payor.participant_code }
        } else {
            payload = { "jwt_token": query.get("jwt_token") }
        }

        var mode;

        if (_.get(payor, "participant_code") === "1-29482df3-e875-45ef-a4e9-592b6f565782") {
            mode = "mock-invalid";
        } else if (process.env.REACT_APP_ENV === "Staging") {
            mode = "mock-valid";
        } else {
            mode = "actual";
        }

        let headers = { "mode": mode }

        sendData("/participant/getInfo", JSON.stringify(payload), headers).then((data => {
            let participant = _.get(data, 'data.participant') || {}
            if (_.size(_.keys(participant)) == 0) {
                toast.info("Details does not exist in payor system, Please enter.", {
                    position: toast.POSITION.TOP_CENTER, autoClose: 3000
                });
            }
            setPrimaryEmail(participant.primary_email || "");
            setPrimaryMobile(participant.primary_mobile || "");
            setParticipantName(participant.participant_name || "");
        })).catch((err => {
            toast.error(_.get(err, 'data.error.message') || "Internal Server Error", {
                position: toast.POSITION.TOP_CENTER
            });
        }))
    }

    const debouncedChangeHandler = useCallback(
        debounce((value => getParticipantDetails({ value, payor })), 2000)
        , [payor]);

    return <>
        <ToastContainer autoClose={false} />
        <Grid centered container>
            <Grid.Row columns="1">
                <div className='form-container' style={{ background: '#63ac84', height: '100px', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <Grid.Column>
                        <Image src='favicon.ico' style={{ width: '50px', marginRight: '20px' }} />
                    </Grid.Column>
                    <Grid.Column>
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Onboarding Basic Info - { process.env.REACT_APP_ENV }</b></p>
                    </Grid.Column>
                </div>
            </Grid.Row>
            <Grid.Row columns="1" >
                <Segment raised padded textAlign='left' className='form-container'>
                    <Form onSubmit={handleSubmit(onSubmit)} className="container">
                        {!isJWTPresent ?
                            <Form.Field>
                                <label>Roles</label>
                            </Form.Field> : null}
                        {!isJWTPresent ?
                            <Form.Field>
                                <input
                                    id="payor"
                                    type="radio"
                                    label='Payor'
                                    name='roles'
                                    value='payor'
                                    {...register("roles", { required: true })}
                                /> Payor
                            </Form.Field> : null}
                        {!isJWTPresent ?
                            <Form.Field>
                                <input
                                    id="provider"
                                    type="radio"
                                    label='Provider'
                                    name='roles'
                                    value='provider'
                                    {...register("roles", { required: true })}
                                /> Provider
                            </Form.Field> : null}
                        {!isJWTPresent && watchRoles === "provider" ?
                            <Form.Field>
                                <label>Payors</label>
                                <Dropdown placeholder='Select Payor' fluid selection options={payorList} onChange={e => getPayor(e?.value)} />
                            </Form.Field>
                            : null
                        }
                        {!isJWTPresent && watchRoles === "provider" ?
                            <Form.Field>
                                <label>Applicant Code</label>
                                <input placeholder='Enter Applicant Code' onInput={debouncedChangeHandler} {...register("applicant_code", { required: false })} />
                            </Form.Field>
                            : null
                        }
                        <Form.Field >
                            <label>Email</label>
                            <input placeholder='Enter Email' value={primaryEmail} disabled={primaryEmail != '' && (watchApplicantCode != '' || isJWTPresent)} onInput={e => setPrimaryEmail(e.target.value)} {...register("primary_email", { required: true, pattern: /^\S+@\S+$/i, message: "Email required" })} />
                        </Form.Field>
                        <Form.Field>
                            <label>Phone Number</label>
                            <input placeholder='Enter Phone Number' value={primaryMobile} disabled={primaryMobile != '' && (watchApplicantCode != '' || isJWTPresent)} onInput={e => setPrimaryMobile(e.target.value)} {...register("primary_mobile", { required: true })} />
                        </Form.Field>
                        <Form.Field>
                            <label>Organisation Name</label>
                            <input placeholder='Enter Organisation Name' value={participantName} disabled={participantName != '' && (watchApplicantCode != '' || isJWTPresent)} onInput={e => setParticipantName(e.target.value)}  {...register("participant_name", { required: true })} />
                        </Form.Field>
                        <Button disabled={sending} type='submit' className='primary center-element'>
                            {sending ? "Submitting" : "Onboard"}
                        </Button>
                    </Form>
                </Segment>
            </Grid.Row>
        </Grid>
    </>

}