import { useEffect, useState } from 'react'
import { Button, Form, Grid, Label, Loader, Segment, TextArea } from 'semantic-ui-react'
import { getToken, post } from './../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import * as _ from 'lodash';
import { maskEmailAddress, replaceString } from '../utils/StringUtil';
import { useHistory } from 'react-router-dom/cjs/react-router-dom.min';
import { useSelector } from 'react-redux';

export const UpdateRegistry = ({ changeTab, formState, setState }) => {

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();
    const [sending, setSending] = useState(false)
    const [loader, setLoader] = useState(false)
    const [token, setToken] = useState("")
    const [certType, setCertType] = useState("")
    let history = useHistory();
    const [formErrors, setFormErrors] = useState({});
    const [passwordType, setPasswordType] = useState("password")
    const formStore = useSelector((state) => state)

    useEffect(() => {
        if (_.get(formState, 'participant') == null) {
            setState({ ...formState, ...formStore.formState })
        }
        console.log('formstate', JSON.stringify(formState))
    }, []);


    const onSubmit = (data) => {
        setSending(true)
        const formData = { "jwt_token": _.get(formState, 'access_token'), participant: { "participant_code": _.get(formState, 'participant_code'), "participant_name": _.get(formState, 'participant.participant_name'), "endpoint_url": data.endpoint_url, "encryption_cert": data.encryption_cert } };
        post("/participant/onboard/update", formData).then((data => {
            reset()
            history.push("/onboarding/success");
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
            <div className='form-main' style={{ marginTop: '20px' }}>
                <Form.Field>
                    <b>Participant Code:</b>&ensp;{maskEmailAddress(_.get(formState, 'participant_code'))}
                </Form.Field>
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
            </div><br /><br />
            <Grid>
                <Grid.Row>
                    <Grid.Column>
                        <Button disabled={sending} type='submit' className="primary center-element button-color">
                                {sending ? "Submitting" : "Submit"}</Button>
                    </Grid.Column>
                </Grid.Row>
            </Grid>
        </Form>
    </>

}