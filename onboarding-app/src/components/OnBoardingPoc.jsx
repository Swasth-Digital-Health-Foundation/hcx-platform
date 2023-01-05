import React, { useState, useEffect } from 'react'
import { Button, Form, Segment, Grid, Image, Radio } from 'semantic-ui-react'
import { sendData } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import { useQuery } from '../service/QueryService';
import Dropdown from 'react-dropdown';
import 'react-dropdown/style.css';
import { getParticipantSearch } from '../service/RegistryService';
import * as _ from 'lodash';

export const OnBoardingPoc = () => {
    const env = 'Local'
    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();

    const [sending, setSending] = useState(false)
    const [payorList, setPayorList] = useState([])
    const [payor, setPayor] = useState(null)
    const [isJWTPresent, setIsJWTPresent] = useState(false)
    const watchRoles = watch("roles", "payor")
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
        if (isJWTPresent){
            formData = [{ "jwt_token": jwtToken, "participant": { ...data, "roles": [data.roles] } }];
        } else if (payor != null) {
            formData = [{ "payor_code": payor.participant_code, "applicant_code": data.applicant_code, "participant": { ...data, "roles": [data.roles] } }];
        } else {
            formData = [{ "participant": { ...data, "roles": [data.roles] } }];
        }

        console.log(formData);
        
        sendData("/participant/verify", JSON.stringify(formData)).then((data => {
            toast.success("Form is submitted successfully", {
                position: toast.POSITION.TOP_CENTER
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

    return <>
        <ToastContainer autoClose={false} />
        <Grid centered container>
            <Grid.Row columns="1">
                <div className='form-container' style={{ background: '#63ac84', height: '100px', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <Grid.Column>
                        <Image src='favicon.ico' style={{width: '50px', marginRight: '20px'}} />
                    </Grid.Column>
                    <Grid.Column>
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Onboarding - POC</b></p>
                    </Grid.Column>
                </div>
            </Grid.Row>
            <Grid.Row columns="1" >
                <Segment raised padded textAlign='left' className='form-container'>
                    <Form onSubmit={handleSubmit(onSubmit)} className="container">
                        <Form.Field >
                            <label>Email</label>
                            <input placeholder='Enter Email' {...register("primary_email", { required: true, pattern: /^\S+@\S+$/i, message: "Email required" })} />
                        </Form.Field>
                        <Form.Field>
                            <label>Phone Number</label>
                            <input placeholder='Enter Phone Number' {...register("primary_mobile", { required: true })} />
                        </Form.Field>
                        <Form.Field>
                            <label>Organisation Name</label>
                            <input placeholder='Enter Organisation Name' {...register("participant_name", { required: true })} />
                        </Form.Field>
                        <Form.Field>
                            <label>Roles</label>
                        </Form.Field>
                        <Form.Field>
                            <input
                                id="payor"
                                type="radio"
                                label='Payor'
                                name='roles'
                                value='payor'
                                {...register("roles", { required: true })}
                            /> Payor
                        </Form.Field>
                        <Form.Field>
                            <input
                                id="provider"
                                type="radio"
                                label='Provider'
                                name='roles'
                                value='provider'
                                {...register("roles", { required: true })}
                            /> Provider
                        </Form.Field>
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
                                <input placeholder='Enter Applicant Code' {...register("applicant_code", { required: false })} />
                            </Form.Field>
                            : null
                        }
                        <Button disabled={sending} type='submit' className='primary center-element'>
                            {sending ? "Submitting" : "Onboard"}
                        </Button>
                    </Form>
                </Segment>
            </Grid.Row>
        </Grid>
    </>

}