import React, { useState, useEffect } from 'react'
import { Button, Form, Grid, Label, Loader } from 'semantic-ui-react'
import { get, post } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import { maskEmailAddress, replaceString } from '../utils/StringUtil';
import * as _ from 'lodash';
import { useSelector } from 'react-redux';
import { regenerateLink } from '../api/RegenerateAPI';

export const OTPVerify = ({ changeTab, formState, setState }) => {

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();
    const apiVersion = process.env.REACT_APP_PARTICIPANT_API_VERSION;
    const [sending, setSending] = useState(false)
    const formStore = useSelector((state) => state)
    const [formErrors, setFormErrors] = useState({});
    const [identityVerification, setIdentityVerfication] = useState('pending');
    const [communicationVerification, setCommunicationVerfication] = useState('pending');

    useEffect(() => {
        if (_.get(formState, 'participant') == null) {
            setState({ ...formState, ...formStore.formState })
        }
        setIdentityVerfication(_.get(formState, 'identity_verification') || "pending")
        setCommunicationVerfication(_.get(formState, 'verificationStatus') || "pending")
    }, []);

    const onSubmit = () => {
        setFormErrors({})
        reset()
        changeTab(2)
    }

    const getVerificationStatus = () => {
        setSending(true)
        get(apiVersion + "/participant/read/" + _.get(formState, 'participant_code') + "?fields=verificationStatus,sponsors")
            .then((function (data) {
                const participant = _.get(data, 'data.participants')[0] || {}
                setIdentityVerfication(_.get(participant, 'sponsors') ? participant.sponsors[0].status : 'pending')
                setCommunicationVerfication(_.get(participant, 'verificationStatus') || "pending")
            })).catch((function (err) {
                console.error(err)
                toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                    position: toast.POSITION.TOP_CENTER
                });
            }))
            .finally(() => {
                setSending(false)
            })
    }

    const regenerate = () => {
        setSending(true);
        const formData = { "participant_code": _.get(formState, 'participant_code'), "participant_name": _.get(formState, 'participant.participant_name'), "primary_email": _.get(formState, 'participant.primary_email'), "primary_mobile": _.get(formState, 'participant.primary_mobile') };
        post("/participant/verification/link/send", formData).then((data => {
            toast.success("Links are regenerated successfully.", {
                position: toast.POSITION.TOP_CENTER, autoClose: 2000
            });
        })).catch(err => {
            toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                position: toast.POSITION.TOP_CENTER, autoClose: 2000
            });
        }).finally(() => {
            setSending(false);
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
                        <b>Identity Verification:</b>&ensp;
                        {identityVerification === 'accepted' ?
                            <Label color='green' horizontal> Accepted </Label>
                            : null}
                        {identityVerification === 'rejected' ?
                            <Label color='red' horizontal> Rejected </Label>
                            : null}
                        {identityVerification === 'pending' ?
                            <Label color='yellow' horizontal> Pending </Label>
                            : null}
                    </Grid.Column>
                    <Grid.Column>
                        <b>Communication Verification:</b>&ensp;
                        {communicationVerification === 'successful' ?
                            <Label color='green' horizontal> Accepted </Label>
                            : null}
                        {communicationVerification === 'failed' ?
                            <Label color='red' horizontal> Rejected </Label>
                            : null}
                        {communicationVerification === 'pending' ?
                            <Label color='yellow' horizontal> Pending </Label>
                            : null}
                    </Grid.Column>
                </Grid.Row>
                {formErrors.error && (<Grid.Row centered><div style={{ "color": "red" }}>{formErrors.error}</div></Grid.Row>)}
                <Grid.Row>
                    <Grid.Column>
                        <Button disabled={sending} type="submit" className="primary center-element button-color">
                            {sending ? "Submitting" : "Submit"}</Button>
                    </Grid.Column>
                </Grid.Row>
                <Grid.Row>
                    {communicationVerification === 'successful' && identityVerification === 'accepted' ? null :
                        <Grid.Column style={{ textAlign: 'left', marginBottom: communicationVerification === 'successful' ? '0px' : '-22px' }}>
                            <a disabled={sending} onClick={getVerificationStatus} href='#'> Refresh</a>
                        </Grid.Column>
                    }
                    {communicationVerification === 'successful' ? null :
                        <Grid.Column style={{ textAlign: 'right' }}>
                            <a disabled={sending} onClick={regenerate} href='#'> Regenerate Link</a>
                        </Grid.Column>
                    }
                </Grid.Row>
            </Grid>
        </Form>
    </>


}