import React, { useState, useEffect, useCallback } from 'react'
import { Button, Form, Segment, Grid, Image, Radio, Dimmer, Loader, Icon } from 'semantic-ui-react'
import { post, sendData } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast, autoClose } from 'react-toastify';
import { useQuery } from '../service/QueryService';
import Dropdown from 'react-dropdown';
import 'react-dropdown/style.css';
import { getParticipantSearch } from '../service/RegistryService';
import * as _ from 'lodash';
import debounce from 'lodash.debounce';


export const BasicDetails = ({ changeTab, formState, setState }) => {
    const { register, handleSubmit, watch, formState: { errors }, reset, setValue } = useForm();
    const [sending, setSending] = useState(false)
    const [payorList, setPayorList] = useState([])
    const [payor, setPayor] = useState({})
    const [isJWTPresent, setIsJWTPresent] = useState(false)
    const [primaryEmail, setPrimaryEmail] = useState("")
    const [primaryMobile, setPrimaryMobile] = useState("")
    const [participantName, setParticipantName] = useState("")
    const [invalidApplicantCode, setInvalidApplicantCode] = useState(false)
    const [loader, setLoader] = useState(false)
    const watchRoles = watch("roles", "payor")
    const watchApplicantCode = watch("applicant_code", "")
    const watchPrimaryEmail = watch("primary_email", "")
    const [applicantCode, setApplicantCode] = useState("")
    const [fetchResponse, setFetchResponse] = useState(false)
    const [formErrors , setFormErrors] = useState({});


    let query = useQuery();
    
    const getPayor = participantName => {
        const participant = payorList.find(participant => participant.participant_name === participantName);
        if (participant) {
            setPayor(participant)
        }

    }

    const setPrimaryEmailState = email => {
        setPrimaryEmail(email);
        setFormErrors({});
    }

    const setPrimaryMobileState = phone => {
        setPrimaryMobile(phone);
        setFormErrors({});
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
        setLoader(true)
        setSending(true)
        const jwtToken = query.get("jwt_token");
        const jwt_token = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJMYU9HdVRrYVpsVEtzaERwUng1R25JaXUwV1A1S3VGUUoyb29WMEZnWGx3In0.eyJleHAiOjE2NzgyNDc4ODEsImlhdCI6MTY3NTY1NTg4MSwianRpIjoiMzQwODdiNDEtMWQyMy00NjlhLWI3NjMtOWVmY2U4Y2FhMzQ4IiwiaXNzIjoiaHR0cDovL2E5ZGQ2M2RlOTFlZTk0ZDU5ODQ3YTEyMjVkYThiMTExLTI3Mzk1NDEzMC5hcC1zb3V0aC0xLmVsYi5hbWF6b25hd3MuY29tOjgwODAvYXV0aC9yZWFsbXMvc3dhc3RoLWhlYWx0aC1jbGFpbS1leGNoYW5nZSIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiJkZmNlMzY3OS02ZDM2LTQ3NzItOGI1NS01NWE1YzQ2MzA2MTAiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJyZWdpc3RyeS1mcm9udGVuZCIsInNlc3Npb25fc3RhdGUiOiI1MGQzZWFlZC1iM2U3LTQ4MGYtYTg4Yy02ZjY4MGZjMWE1ZDMiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vbG9jYWxob3N0OjQyMDIiLCJodHRwOi8vbG9jYWxob3N0OjQyMDIiLCJodHRwczovL2xvY2FsaG9zdDo0MjAwIiwiaHR0cHM6Ly9uZGVhci54aXYuaW4iLCJodHRwOi8vbG9jYWxob3N0OjQyMDAiLCJodHRwOi8vbmRlYXIueGl2LmluIiwiaHR0cDovLzIwLjE5OC42NC4xMjgiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIkhJRS9ISU8uSENYIiwiZGVmYXVsdC1yb2xlcy1uZGVhciJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoiaGN4QHN3YXN0aGFwcC5vcmciLCJlbnRpdHkiOlsiT3JnYW5pc2F0aW9uIl0sImVtYWlsIjoiaGN4QHN3YXN0aGFwcC5vcmcifQ.Hv-hvHI_ZbfbX2o5oQXbmWAQkFgzRnuDkHz2MIdUVXola7ImWMdUGIENJxByjJ_zTZChQGJpX1CsHXvJ_vWDkeVRI0wXwSMGl-0_o3A588Lu47XTpXKLuaGlyzarsynCfZAEa6KakuDV-wzepDJDYFimgN5YsShmjPoW8Zw6sRMbZCqz_kM8EYFJRzVBy16YVWB9PXXjZfmbFxqh43O3rgIcPjxjt2I8m_lWlFb4UVFfg9txv4bReq3qlxs75ABD3tKPDrZNedL7yGEWWXelfTw3o9U4lwUrMrrS-5SH9NufMpZ5irRLYCJs1Y-sj0urX2Bgb8lcSosoQZ-j7jPYmQ";
        let formData;

        if (isJWTPresent) {
            formData = [{ "type": "onboard-through-jwt", "jwt": jwtToken, "participant": { "participant_name": data.participant_name, "primary_email": data.primary_email, "primary_mobile": data.primary_mobile, "roles": ["provider"] } }];
        } else if (payor != null && !invalidApplicantCode) {
            formData = [{ "type": "onboard-through-verifier", "verifier_code": payor.participant_code, "applicant_code": data.applicant_code, "participant": { "participant_name": data.participant_name, "primary_email": data.primary_email, "primary_mobile": data.primary_mobile, "roles": ["provider"] } }];
        } else {
            formData = [{ "participant": { "participant_name": data.participant_name, "primary_email": data.primary_email, "primary_mobile": data.primary_mobile, "roles": [data.roles] } }];
        }

        console.log("i came in verify");
        post("/participant/verify", JSON.stringify(formData))
            .then((data => {
                console.log("result of verify", data);
                toast.success("Form is submitted successfully", {
                    position: toast.POSITION.TOP_CENTER, autoClose: 2000
                });
                reset();
                changeTab(1)
                setState({ ...formState, ...(formData[0]), ...{ "participant_code": _.get(data, 'data.result.participant_code'), "identity_verification": _.get(data, 'data.result.identity_verification') } })
            })).catch(err => {
                // toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                //     position: toast.POSITION.TOP_CENTER
                // });
                console.log(_.get(err, 'response.data.error.message') );
                if(_.get(err, 'response.data.error.message') && _.get(err, 'response.data.error.message') == "Username already invited / registered for Organisation"){
                    setFormErrors({email:_.get(err, 'response.data.error.message'), phone:_.get(err, 'response.data.error.message')});
                }else{
                toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                     position: toast.POSITION.TOP_CENTER
                });     
                }
                
            }).finally(() => {
                setSending(false)
                setLoader(false)
            })
    }


    const getParticipantDetails = () => {
        setLoader(true)
        let payload;
        if (applicantCode && payor) {
            payload = { "applicant_code": applicantCode, "verifier_code": payor.participant_code }
        } else {
            payload = { "jwt_token": query.get("jwt_token") }
        }

        var mode;

        if (_.get(payor, "participant_code") === "1-29482df3-e875-45ef-a4e9-592b6f565782") {
            mode = "mock-valid";
        } else if (process.env.REACT_APP_ENV === "Staging") {
            mode = "mock-invalid";
        } else {
            mode = "actual";
        }

        let headers = { "mode": mode }

        post("/participant/getInfo", JSON.stringify(payload), headers).then((data => {
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
            setFetchResponse(true);
        })).catch((err => {
            let errMsg = _.get(err, 'response.data.error.message')
            if (typeof errMsg === 'string' && errMsg.includes('UnknownHostException')) {
                errMsg = 'Payor system in unavailable, Please try later!'
            }
            toast.error(errMsg || "Internal Server Error", {
                position: toast.POSITION.TOP_CENTER
            });
            setInvalidApplicantCode(true);
            setFetchResponse(true);
        })).finally(() => {
            setLoader(false)
        })
    }

    return <>
        <ToastContainer autoClose={false} />
        <Form disabled={sending} onSubmit={handleSubmit(onSubmit)} className="container">
            {loader && <Loader active />}
            <div className='form-main' style={{ marginTop: '15px' }}>
                <Grid columns='equal'>
                    {!isJWTPresent ?
                        <Form.Field disabled={sending} className={{ 'error': 'roles' in errors }} required>
                            <label>Roles:</label>
                        </Form.Field> : null}
                    {!isJWTPresent ?
                        <Form.Field  disabled={sending}>
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
                {!isJWTPresent && watchRoles === "provider" ?
                    <Form.Field disabled={sending} className={{ 'error': _.size(_.keys(errors)) > 0 && _.size(_.keys(payor)) == 0 }} required>
                        <label>Payors</label>
                        <Dropdown className='input-text' placeholder='Select Payor' fluid selection options={payorList} onChange={e => getPayor(e?.value)} required />
                    </Form.Field>
                    : null
                }
                {!isJWTPresent && watchRoles === "provider" ?
                    <Grid.Row>
                        <Form.Field disabled={fetchResponse} style = {{ marginBottom: '15px' }} className={{ 'error': 'applicant_code' in errors }} required>
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
                    <Form.Field disabled={sending} className={{ 'error': primaryEmail === '' && 'primary_email' in errors }} required>
                        <label>Email</label>
                        <input className='input-text' type='email' placeholder='Enter Email' value={primaryEmail} disabled={primaryEmail != '' && ((watchApplicantCode != '' && invalidApplicantCode) || isJWTPresent)} onInput={e => setPrimaryEmailState(e.target.value)} {...register("primary_email", { required: true, pattern: /^\S+@\S+$/i, message: "Email required" })} />
                        {formErrors.email && (<div style={{"color":"red"}}>This email address already exists</div>)}
                    </Form.Field> : null}
                {watchRoles === "payor" || fetchResponse === true ?
                    <Form.Field disabled={sending} className={{ 'error': primaryMobile === '' && 'primary_mobile' in errors }} required>
                        <label>Phone Number</label>
                        <input className='input-text' placeholder='Enter Phone Number' value={primaryMobile} disabled={primaryMobile != '' && ((watchApplicantCode != '' && invalidApplicantCode) || isJWTPresent)} onInput={e => setPrimaryMobileState(e.target.value)} {...register("primary_mobile", { required: true, pattern: /^[0-9]{10}/i })} />
                        {formErrors.phone && (<div style={{"color":"red"}}>This phone number already exists</div>)}
                    </Form.Field> : null}
                {watchRoles === "payor" || fetchResponse === true ?
                    <Form.Field disabled={sending} className={{ 'error': participantName === '' && 'participant_name' in errors }} required>
                        <label>Organisation Name</label>
                        <input className='input-text' placeholder='Enter Organisation Name' value={participantName} disabled={participantName != '' && ((watchApplicantCode != '' && invalidApplicantCode) || isJWTPresent)} onInput={e => setParticipantName(e.target.value)}  {...register("participant_name", { required: true })} />
                    </Form.Field> : null}
            </div><br />
            {watchRoles === "payor" || fetchResponse === true ?
                <Button className={{ 'disabled': sending, 'primary center-element button-color': true }} disabled={sending} type='submit'>
                    {sending ? "Submitting" : "Submit"}
                </Button> : null}
        </Form>
    </>

}