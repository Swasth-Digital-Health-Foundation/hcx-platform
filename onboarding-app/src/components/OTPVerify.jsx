import React, { useState, useEffect } from 'react'
import { Button, Form, Grid, Loader } from 'semantic-ui-react'
import { post } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import { maskEmailAddress, replaceString } from '../utils/StringUtil';
import * as _ from 'lodash';
import { useSelector } from 'react-redux';

export const OTPVerify = ({ changeTab, formState, setState }) => {

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();
    const [sending, setSending] = useState(false)
    const formStore = useSelector((state) => state)
    const [formErrors , setFormErrors] = useState({});

    useEffect(() => {
        if (_.get(formState, 'participant') == null) {
            setState({ ...formState, ...formStore.formState })
        }
    }, []);

    const onSubmit = (data) => {
        setSending(true)
        setFormErrors({})
        const formData = { 'participant_code' : _.get(formState, 'participant_code'), 'verifier_code' : _.get(formState, 'verifier_code') || "", 'otpVerification' : [ { 'channel': 'email', 'otp': data.email_otp }, { 'channel' : 'phone', "otp": data.phone_otp }]};
        post("/applicant/verify", formData).then((data => {
            toast.success("Form is submitted successfully", {
                position: toast.POSITION.TOP_CENTER, autoClose: 2000
            });
            reset()
            changeTab(2)
        })).catch(err => {
            if(_.get(err, 'response.data.error.message')){
                setFormErrors({error:_.get(err, 'response.data.error.message')});
            }else{
                toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                    position: toast.POSITION.TOP_CENTER
               });  
            }
        }).finally(() => {
            setSending(false)
        })
    }

    const regenerateOTP = () => {
        setSending(true)
        const formData = { "participant_code": _.get(formState, 'participant_code'), "participant_name": _.get(formState, 'participant.participant_name'), "primary_email": _.get(formState, 'participant.primary_email'), "primary_mobile": _.get(formState, 'participant.primary_mobile') };
        post("/participant/otp/send", formData).then((data => {
            toast.success("OTPs are successfully regenerated.", {
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

    return <>
        <ToastContainer autoClose={false} />
            <Form onSubmit={handleSubmit(onSubmit)} className="container">
                {sending && <Loader active />}
                <Grid columns='equal'>
                    <Grid.Row>
                        <Grid.Column>
                            <b>Email:</b>&ensp;{maskEmailAddress(_.get(formState, 'participant.primary_email'))}
                        </Grid.Column>
                        <Grid.Column>
                            <b>Phone Number:</b>&ensp;{replaceString(_.get(formState, 'participant.primary_mobile') || "", 7, "*")}
                        </Grid.Column>
                    </Grid.Row>
                    <Grid.Row>
                        <Grid.Column>
                            <b>Identity Verification:</b>&ensp; {_.get(formState, 'identity_verification')} &ensp;
                        </Grid.Column>
                    </Grid.Row>
                    <Grid.Row style={{ width: "70px" }}>
                        <Grid.Column>
                            <Form.Field disabled={sending} className={{ 'error': 'email_otp' in errors }} required>
                                <label>Email OTP</label>
                                <input placeholder='Enter Email OTP' {...register("email_otp", { required: true })} />
                            </Form.Field>
                        </Grid.Column>
                        <Grid.Column>
                            <Form.Field disabled={sending} className={{ 'error': 'phone_otp' in errors }} required>
                                <label>Phone OTP</label>
                                <input placeholder='Enter Phone OTP' {...register("phone_otp", { required: true })} />
                            </Form.Field>
                        </Grid.Column>
                    </Grid.Row>
                    {formErrors.error && (<Grid.Row centered><div style={{"color":"red"}}>{formErrors.error}</div></Grid.Row>)}
                    <Grid.Row>
                        <Grid.Column>
                            <Button disabled={sending} type="submit" className="primary center-element button-color">
                                {sending ? "Submitting" : "Submit"}</Button>
                        </Grid.Column>
                    </Grid.Row>
                    <Grid.Row>
                        <Grid.Column style={{ textAlign: 'right' }}>
                            <a disabled={sending} onClick={regenerateOTP} href='#'> Regenerate OTP</a>
                        </Grid.Column>
                    </Grid.Row>
                </Grid>
            </Form>
    </>


}