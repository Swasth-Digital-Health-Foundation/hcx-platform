import { useEffect, useState } from 'react'
import { Button, Form, Grid, Loader } from 'semantic-ui-react'
import { getToken, post } from './../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import * as _ from 'lodash';
import { replaceString } from '../utils/StringUtil';
import { useHistory } from 'react-router-dom/cjs/react-router-dom.min';
import { useSelector } from 'react-redux';

export const UpdateRegistry = ({ changeTab, formState, setState }) => {

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();
    const [sending, setSending] = useState(false)
    const [loader, setLoader] = useState(false)
    const [token, setToken] = useState("")
    const [passwordVerified, setPasswordVerified] = useState(false)
    const [password, setPassword] = useState("")
    let history = useHistory();

    const formStore = useSelector((state) => state)

    useEffect(() => {
        if (_.get(formState, 'participant') == null) {
            setState({ ...formState, ...formStore.formState })
        }
    }, []);


    const onSubmit = (data) => {
        setLoader(true)
        setSending(true)
        const formData = { "jwt_token": token, participant: { "participant_code": _.get(formState, 'participant_code'), "participant_name": _.get(formState, 'participant.participant_name'), "endpoint_url": data.endpoint_url, "certificates_type": data.certificates_type, "encryption_cert_path": data.encryption_cert_path, "signing_cert_path": data.signing_cert_path } };
        post("/participant/onboard/update", formData).then((data => {
            toast.success("Form is submitted successfully", {
                position: toast.POSITION.TOP_CENTER, autoClose: 2000
            });
            reset()
            setTimeout(function () {
                history.push("/onboarding/end");
            }, 2000);
        })).catch(err => {
            toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                position: toast.POSITION.TOP_CENTER
            });
        }).finally(() => {
            setSending(false)
            setLoader(false)
        })
    }

    const getAccessToken = () => {
        setSending(true)
        setLoader(true)
        let body = { "client_id": "registry-frontend", "username": _.get(formState, 'participant.primary_email'), "password": password, "grant_type": "password" }
        getToken("/auth/realms/swasth-health-claim-exchange/protocol/openid-connect/token", body)
            .then((data => {
                setToken(_.get(data, 'data.access_token'))
                toast.success("Password is verifed", {
                    position: toast.POSITION.TOP_CENTER, autoClose: 2000
                });
                setPasswordVerified(true);
            })).catch((err => {
                let errMsg = _.get(err, 'response.data.error_description')
                toast.error(errMsg || "Internal Server Error", {
                    position: toast.POSITION.TOP_CENTER
                });
            }))
            .finally(() => {
                setLoader(false)
                setSending(false)
            })
    }

    return <>
        <ToastContainer autoClose={false} />
        <Form onSubmit={handleSubmit(onSubmit)} className="container">
            {loader && <Loader active />}
            <div className='form-main' style={{ marginTop: '20px' }}>
                <Form.Field>
                    <b>Participant Code:</b>&ensp;{replaceString(_.get(formState, 'participant_code') || "", 5, "X")}
                </Form.Field>
                <Form.Field>
                    <b>Username:</b>&ensp;{replaceString(_.get(formState, 'participant.primary_email') || "", 5, "X")}
                </Form.Field>
                <Form.Field disabled={passwordVerified} className={{ 'error': 'password' in errors }} required>
                    <label>Password</label>
                    <input className='input-text' onInput={e => setPassword(e.target.value)} type='password' placeholder='Enter Password' {...register("password", { required: true })} />
                </Form.Field>
                {passwordVerified ? null :
                    <Button disabled={sending} onClick={getAccessToken} className="primary center-element button-color">
                        {sending ? "Verifying" : "Verify"}</Button>}
                {passwordVerified ?
                    <Form.Field disabled={sending} className={{ 'error': 'endpoint_url' in errors }} required>
                        <label>Endpoint URL</label>
                        <input className='input-text' placeholder='Enter Endpoint URL' {...register("endpoint_url", { required: true })} />
                    </Form.Field> : null}
                {passwordVerified ?
                    <Form.Field disabled={sending} className={{ 'error': 'certificates_type' in errors }} required>
                        <label>Certificates Type:</label>
                    </Form.Field> : null}
                {passwordVerified ?
                    <Form.Field disabled={sending}>
                        <input
                            id="URL"
                            type="radio"
                            label='URL'
                            name='certificates_type'
                            value='URL'
                            {...register("certificates_type", { required: true })}
                        /> URL
                    </Form.Field> : null}
                {passwordVerified ?
                    <Form.Field disabled={sending}>
                        <input
                            id="Text"
                            type="radio"
                            label='Text'
                            name='certificates_type'
                            value='Text'
                            {...register("certificates_type", { required: true })}
                        /> Text
                    </Form.Field> : null}
                {passwordVerified ?
                    <Form.Field disabled={sending} className={{ 'error': 'encryption_cert_path' in errors }} required>
                        <label>Encryption Cert Path</label>
                        <input placeholder='Enter Encryption Cert Path' {...register("encryption_cert_path", { required: true })} />
                    </Form.Field> : null}
                {passwordVerified ?
                    <Form.Field disabled={sending} className={{ 'error': 'signing_cert_path' in errors }} required>
                        <label>Signing Cert Path</label>
                        <input placeholder='Enter Signing Cert Path' {...register("signing_cert_path", { required: true })} />
                    </Form.Field> : null}
            </div><br /><br />
            <Grid>
                <Grid.Row>
                    <Grid.Column>
                        {passwordVerified ?
                            <Button disabled={sending} type='submit' className="primary center-element button-color">
                                {sending ? "Submitting" : "Submit"}</Button> : null}
                    </Grid.Column>
                </Grid.Row>
            </Grid>
        </Form>
    </>

}