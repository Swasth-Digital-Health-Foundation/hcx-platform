import { useEffect, useState } from 'react'
import { Button, Form, Segment, Grid, Image, Radio, Loader } from 'semantic-ui-react'
import { post } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import { replaceString } from '../utils/StringUtil';
import * as _ from 'lodash';
import { useSelector } from 'react-redux';

export const OTPVerify = ({ changeTab, formState, setState }) => {

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();

    const [sending, setSending] = useState(false)
    const [loader, setLoader] = useState(false)

    const formStore = useSelector((state) => state)

    useEffect(() => {
        if(_.get(formState, 'participant') == null) {
            setState({ ...formState, ...formStore.formState})
        }
    }, []);

    const onSubmit = (data) => {
        setLoader(true)
        setSending(true)
        const formData = [{ "type": "email-otp-validation", "primary_email": _.get(formState, 'participant.primary_email'), "otp": data.email_otp }, { "type": "mobile-otp-validation", "primary_mobile": _.get(formState, 'participant.primary_mobile'), "otp": data.phone_otp }];
        post("/participant/verify", formData).then((data => {
            toast.success("Form is submitted successfully", {
                position: toast.POSITION.TOP_CENTER, autoClose: 2000
            });
            reset()
            changeTab(2)
        })).catch(err => {
            toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                position: toast.POSITION.TOP_CENTER
            });
        }).finally(() => {
            setSending(false)
            setLoader(false)
        })
    }

    const regenerateOTP = () => {
        setLoader(true)
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
            setLoader(false)
        })
    }

    return <>
        <ToastContainer autoClose={false} />
        <Form onSubmit={handleSubmit(onSubmit)} className="container">
            {loader && <Loader active />}
            <Grid columns='equal'>
                <Grid.Row>
                    <Grid.Column>
                        <b>Email:</b>&ensp;{replaceString(_.get(formState, 'participant.primary_email') || "", 5, "X")}
                    </Grid.Column>
                </Grid.Row>
                <Grid.Row>
                    <Grid.Column>
                        <b>Phone Number:</b>&ensp;{replaceString(_.get(formState, 'participant.primary_mobile') || "", 5, "X")}
                    </Grid.Column>
                </Grid.Row>
                <Grid.Row>
                    <Grid.Column>
                        <b>Identity Verification:</b>&ensp; {_.get(formState, 'identity_verification')}
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
                <Grid.Row>
                    <Grid.Column>
                        <Button disabled={sending} type='submit' className="primary center-element button-color">
                            {sending ? "Submitting" : "Submit"}</Button>
                    </Grid.Column>
                </Grid.Row>
                <Grid.Row>
                <Grid.Column>
                        <Button disabled={sending} onClick={regenerateOTP} className="primary center-element button-color">
                            {sending ? "Regenerating" : "Regenerate OTP"}</Button>
                    </Grid.Column>
                </Grid.Row>
            </Grid>
        </Form>
    </>


}