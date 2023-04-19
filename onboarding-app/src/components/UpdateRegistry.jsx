import { useEffect, useState } from 'react'
import { Button, Divider, Form, Grid, Header, Label, Loader, Message } from 'semantic-ui-react'
import { get, post } from './../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import * as _ from 'lodash';
import { useHistory } from 'react-router-dom/cjs/react-router-dom.min';
import { useSelector } from 'react-redux';

export const UpdateRegistry = ({ changeTab, formState, setState }) => {

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();
    const [sending, setSending] = useState(false)
    const [certType, setCertType] = useState("")
    let history = useHistory();
    const formStore = useSelector((state) => state)
    const apiVersion = process.env.REACT_APP_PARTICIPANT_API_VERSION;
    const [identityVerification, setIdentityVerfication] = useState('pending');
    const [communicationVerification, setCommunicationVerfication] = useState('pending');
    const [inactive, setInactive] = useState(false);
    const [emailVerified, setEmailVerified] = useState('pending')
    const [phoneVerified, setPhoneVerified] = useState('pending')

    console.log(inactive)

    useEffect(() => {
        if (_.get(formState, 'participant') == null) {
            setState({ ...formState, ...formStore.formState })
        }

        (async () => {
            let participantCode = _.get(formState, 'participant_code') || formStore.formState.participant_code
            const reqBody = { filters: { participant_code: { 'eq': participantCode } } };
            await post("/applicant/search?fields=communication,sponsors", reqBody)
                .then((async function (data) {
                    let participant = _.get(data, 'data.participants')[0] || {}
                    if (participant) {
                        setIdentityVerfication(_.get(participant, 'sponsors') ? participant.sponsors[0].status : 'pending')
                        setCommunicationVerfication(_.get(participant, 'communication.status') || "pending")
                        setEmailStatus(_.get(participant, 'communication.status'), _.get(participant, 'communication.emailVerified'));
                        setPhoneStatus(_.get(participant, 'communication.status'), _.get(participant, 'communication.phoneVerified'));
                        if (_.get(participant, 'endpoint_url') !== 'http://testurl/v0.7' && (identityVerification !== 'accepted' || communicationVerification !== 'successful')) {
                            setInactive(true)
                        }
                    }
                })).catch((function (err) {
                    console.error(err)
                    toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                        position: toast.POSITION.TOP_CENTER
                    });
                }))
        })()
    }, []);


    const onSubmit = (data) => {
        setSending(true)
        const formData = { "jwt_token": _.get(formState, 'access_token'), participant: { "participant_code": _.get(formState, 'participant_code'), "participant_name": _.get(formState, 'participant.participant_name'), "endpoint_url": data.endpoint_url, "encryption_cert": data.encryption_cert } };
        post("/participant/onboard/update", formData).then((response => {
            const result = _.get(response, 'data.result') || {}
            console.log('result ', result)

            setIdentityVerfication(_.get(response, 'data.result.identity_verification'))
            setCommunicationVerfication(_.get(response, 'data.result.communication_verification'))

            if (identityVerification === 'accepted' && communicationVerification === 'successful') {
                history.push("/onboarding/success");
            } else {
                setInactive(true);
            }

        })).catch(err => {
            toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                position: toast.POSITION.TOP_CENTER
            });
        }).finally(() => {
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

    const getVerificationStatus = () => {
        setSending(true)
        const reqBody = { filters: { participant_code: { 'eq': _.get(formState, 'participant_code') } } };
        post("/applicant/search?fields=communication,sponsors", reqBody)
            .then((function (data) {
                const participant = _.get(data, 'data.participants')[0] || {}
                setIdentityVerfication(_.get(participant, 'sponsors') ? participant.sponsors[0].status : 'pending')
                setCommunicationVerfication(_.get(participant, 'communication.status') || "pending")
                setEmailStatus(_.get(participant, 'communication.status'), _.get(participant, 'communication.emailVerified'));
                setPhoneStatus(_.get(participant, 'communication.status'), _.get(participant, 'communication.phoneVerified'));
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

    const setPhoneStatus = (commStatus, phoneStatus) => {
        if (phoneStatus === null) {
            setPhoneStatus('disabled')
        } else if (commStatus === 'failed' && phoneStatus === false){
            setPhoneVerified('failed')
        } else if (phoneStatus === true) {
            setPhoneVerified('successful')
        }
    }

    const setEmailStatus = (commStatus, emailStatus) => {
        if (emailStatus === null) {
            setEmailStatus('disabled')
        }else if(commStatus === 'failed' && emailStatus === false){
            console.log('inside')
            setEmailVerified('failed')
        } else if (emailStatus === true) {
            setEmailVerified('successful')
        }
    }

    const getIdentityVerification = () => {
        if (identityVerification === 'accepted') {
            return <Label color='green' horizontal> Accepted </Label>;
        } else if (identityVerification === 'rejected') {
            return <Label color='red' horizontal> Rejected </Label>;
        } else if (identityVerification === 'pending') {
            return <Label color='yellow' horizontal> Pending </Label>;
        } else {
            return null;
        }
    }

    const getStatus = (type) => {
        if (type === 'successful') {
            return <Label color='green' horizontal> Successful </Label>;
        } else if (type === 'failed') {
            return <Label color='red' horizontal> Failed </Label>;
        } else if (type === 'pending') {
            return <Label color='yellow' horizontal> Pending </Label>;
        } else {
            return null;
        }
    }

    return <>
        <ToastContainer autoClose={false} />
        <Form onSubmit={handleSubmit(onSubmit)} className="container">
            {sending && <Loader active />}
            {inactive ?
                <Grid.Row>
                    <Message disabled={sending}>
                        <Message.Content style={{ textAlign: 'left' }}>Please complete the communication and identity verification to activate the participant.</Message.Content>
                    </Message>
                    <div className='form-main' style={{ marginBottom: '20px' }}>
                        <Divider horizontal>
                            <Header as='h4'>
                                Verification
                            </Header>
                        </Divider>
                        <Form.Field>
                            <b>Identity:</b>&ensp;
                            {getIdentityVerification()}
                            <b>Email:</b>&ensp;
                            {getStatus(emailVerified)}
                            <b>Phone:</b>&ensp;
                            {getStatus(phoneVerified)}
                        </Form.Field>
                    </div>
                </Grid.Row>
                :
                <Grid.Row>
                    <div className='form-main' style={{ marginTop: '20px' }}>
                        <Divider horizontal>
                            <Header as='h4'>
                                Verification
                            </Header>
                        </Divider>
                        <Form.Field>
                            <b>Identity:</b>&ensp;
                            {getIdentityVerification()}
                            <b>Email:</b>&ensp;
                            {getStatus(emailVerified)}
                            <b>Phone:</b>&ensp;
                            {getStatus(phoneVerified)}
                        </Form.Field>
                        <Divider horizontal>
                            <Header as='h4'>
                                Update Details
                            </Header>
                        </Divider>
                        <Form.Field disabled={sending} className={{ 'error': 'endpoint_url' in errors }} required>
                            <label>Endpoint URL</label>
                            <input className='input-text' placeholder='Enter Endpoint URL' {...register("endpoint_url", { required: true })} />
                        </Form.Field>
                        <Grid columns='equal' style={{ marginTop: '10px' }}>
                            <Form.Field disabled={sending} className={{ 'error': 'certificates_type' in errors }} required>
                                <label>Certificates Type</label>
                            </Form.Field>
                            <Form.Field disabled={sending}>
                                <input
                                    id="URL"
                                    type="radio"
                                    label='URL'
                                    name='certificates_type'
                                    value='URL'
                                    onClick={e => setCertType('URL')}
                                    {...register("certificates_type", { required: true })}
                                /> URL
                            </Form.Field>
                            <Form.Field disabled={sending}>
                                <input
                                    id="certificate_data"
                                    type="radio"
                                    label='Certificate Data'
                                    name='certificates_type'
                                    value='certificate_data'
                                    onClick={e => setCertType('certificate_data')}
                                    {...register("certificates_type", { required: true })}
                                /> Certificate Data
                            </Form.Field>
                        </Grid>
                        {certType ?
                            <Form.Field disabled={sending} className={{ 'error': 'encryption_cert' in errors }} required>
                                <label>Encryption Cert</label>
                                {certType === 'URL' ?
                                    <input placeholder='Enter Encryption Cert Path' {...register("encryption_cert", { required: true })} />
                                    : null}
                                {certType === 'certificate_data' ?
                                    <textarea rows="5" placeholder='-----BEGIN CERTIFICATE-----&#10;Certificate Data&#10;-----END CERTIFICATE-----' {...register("encryption_cert", { required: true })}></textarea>
                                    : null}
                            </Form.Field> : null}
                        {/*Hidden signing cert path as it is optional as per v0.7 spec*/}
                        {/*{certType ?
                    <Form.Field disabled={sending} className={{ 'error': 'signing_cert_path' in errors }} required>
                        <label>Signing Cert</label>
                        {certType === 'URL' ?
                            <input placeholder='Enter Signing Cert Path' {...register("signing_cert_path", { required: true })} />
                            : null}
                        {certType === 'certificate_data' ?
                            <textarea rows="5" placeholder='-----BEGIN CERTIFICATE-----&#10;Certificate Data&#10;-----END CERTIFICATE-----' {...register("signing_cert_path", { required: true })}></textarea>
                            : null}
                    </Form.Field> : null} */}
                    </div><br />
                </Grid.Row>
            }
            <Button disabled={sending} type='submit' className="primary center-element button-color">
                {sending ? "Submitting" : "Submit"}</Button>
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
        </Form>
    </>

}