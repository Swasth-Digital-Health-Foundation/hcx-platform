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
    const { register, handleSubmit, watch, formState: { errors }, reset, setValue } = useForm();
    const [sending, setSending] = useState(false)
    const [payorList, setPayorList] = useState([])
    const [payor, setPayor] = useState({})
    const [isJWTPresent, setIsJWTPresent] = useState(false)
    const [primaryEmail, setPrimaryEmail] = useState("")
    const [primaryMobile, setPrimaryMobile] = useState("")
    const [participantName, setParticipantName] = useState("")
    const [invalidApplicantCode, setInvalidApplicantCode] = useState(false)
    const watchRoles = watch("roles", "payor")
    const watchApplicantCode = watch("applicant_code", "")
    const watchPrimaryEmail = watch("primary_email", "")

    // console.log("value", watchPrimaryEmail , primaryEmail)

    let query = useQuery();

    console.log(errors);

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
            formData = [{ "jwt_token": jwtToken, "participant": { "participant_name": data.participant_name, "primary_email": data.primary_email, "primary_mobile": data.primary_mobile, "roles": ["provider"] } }];
        } else if (payor != null) {
            formData = [{ "payor_code": payor.participant_code, "applicant_code": data.applicant_code, "participant": { "participant_name": data.participant_name, "primary_email": data.primary_email, "primary_mobile": data.primary_mobile, "roles": [data.roles] } }];
        } else {
            formData = [{ "participant": { "participant_name": data.participant_name, "primary_email": data.primary_email, "primary_mobile": data.primary_mobile, "roles": [data.roles] } }];
        }

        sendData("/participant/verify", JSON.stringify(formData))
            .then((data => {
                toast.success("Form is submitted successfully", {
                    position: toast.POSITION.TOP_CENTER, autoClose: 2000
                });
                reset();
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
            setValue("primary_email", participant.primary_email);
            setValue("primary_mobile", participant.primary_mobile);
            setValue("participant_name", participant.participant_name);
        })).catch((err => {
            let errMsg = _.get(err, 'response.data.error.message')
            if (typeof errMsg === 'string' && errMsg.includes('UnknownHostException')) {
                errMsg = 'Payor system in unavailable, Please try later!'
            }
            toast.error(errMsg || "Internal Server Error", {
                position: toast.POSITION.TOP_CENTER
            });
            setInvalidApplicantCode(true);
        }))
    }

    const debouncedChangeHandler = useCallback(
        debounce((value => getParticipantDetails({ value, payor })), 2000)
        , [payor]);

    return <>
            <ToastContainer autoClose={false} />
            <Grid centered container>
                {/* <Grid.Row columns="1">
                    <div className='form-container banner'>
                        <Grid.Column>
                            <Image src='favicon.ico' style={{ width: '50px', marginRight: '20px' }} />
                        </Grid.Column>
                        <Grid.Column>
                            <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Onboarding Basic Info - {process.env.REACT_APP_ENV}</b></p>
                        </Grid.Column>
                    </div>
                </Grid.Row> */}
                <Grid.Row columns="1" >
                    <Segment raised padded textAlign='left' className='form-container'>
                        <Form onSubmit={handleSubmit(onSubmit)} className="container">
                            <div className='form-main'>
                                <Grid columns='equal'>
                                    <Grid.Row columns={3}>
                                        <Grid.Column width={3}>
                                            {!isJWTPresent ?
                                                <Form.Field className={{ 'error': 'roles' in errors }} required>
                                                    <label>Roles:</label>
                                                </Form.Field> : null}
                                        </Grid.Column>
                                        <Grid.Column width = {5}>
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
                                        </Grid.Column>
                                    </Grid.Row>
                                </Grid>
                                {!isJWTPresent && watchRoles === "provider" ?
                                    <Form.Field className={{ 'error': _.size(_.keys(errors)) > 0 && _.size(_.keys(payor)) == 0 }} required>
                                        <label>Payors</label>
                                        <Dropdown className='input-text' placeholder='Select Payor' fluid selection options={payorList} onChange={e => getPayor(e?.value)} required />
                                    </Form.Field>
                                    : null
                                }
                                {!isJWTPresent && watchRoles === "provider" ?
                                    <Form.Field className={{ 'error': 'applicant_code' in errors }} required>
                                        <label>Applicant Code</label>
                                        <input className='input-text' placeholder='Enter Applicant Code' onInput={debouncedChangeHandler} {...register("applicant_code", { required: true })} />
                                    </Form.Field>
                                    : null
                                }
                                <Form.Field className={{ 'error': primaryEmail === '' && 'primary_email' in errors }} required>
                                    <label>Email</label>
                                    <input className='input-text' type='email' placeholder='Enter Email' value={primaryEmail} disabled={primaryEmail != '' && ((watchApplicantCode != '' && invalidApplicantCode) || isJWTPresent)} onInput={e => setPrimaryEmail(e.target.value)} {...register("primary_email", { required: true, pattern: /^\S+@\S+$/i, message: "Email required" })} />
                                </Form.Field>
                                <Form.Field className={{ 'error': primaryMobile === '' && 'primary_mobile' in errors }} required>
                                    <label>Phone Number</label>
                                    <input className='input-text' placeholder='Enter Phone Number' value={primaryMobile} disabled={primaryMobile != '' && ((watchApplicantCode != '' && invalidApplicantCode) || isJWTPresent)} onInput={e => setPrimaryMobile(e.target.value)} {...register("primary_mobile", { required: true, pattern: /^[0-9]{10}/i })} />
                                </Form.Field>
                                <Form.Field className={{ 'error': participantName === '' && 'participant_name' in errors }} required>
                                    <label>Organisation Name</label>
                                    <input className='input-text' placeholder='Enter Organisation Name' value={participantName} disabled={participantName != '' && ((watchApplicantCode != '' && invalidApplicantCode) || isJWTPresent)} onInput={e => setParticipantName(e.target.value)}  {...register("participant_name", { required: true })} />
                                </Form.Field>
                            </div><br />
                            <Button className={{ 'disabled': sending, 'primary center-element': true }} disabled={sending} type='submit'>
                                {sending ? "Submitting" : "Onboard"}
                            </Button>
                        </Form>
                    </Segment>
                </Grid.Row>
            </Grid>
    </>

}