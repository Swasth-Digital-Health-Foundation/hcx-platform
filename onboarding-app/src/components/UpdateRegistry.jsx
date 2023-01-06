import { useState } from 'react'
import { Button, Form, Segment, Grid, Image, Radio } from 'semantic-ui-react'
import { sendData } from './../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import { useQuery } from './../service/QueryService';
import * as _ from 'lodash';

export const UpdateRegistry = () => {

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();

    const [sending, setSending] = useState(false)

    let query = useQuery();

    const onSubmit = (data) => {
        setSending(true)
        const formData = { "jwt_token": data.jwt_token, participant: { "participant_code": query.get("participant_code"), "endpoint_url": data.endpoint_url, "encryption_cert_path": data.encryption_cert_path, "signing_cert_path": data.signing_cert_path } };
        sendData("/participant/onboard/update", formData).then((data => {
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
                        <Image src='favicon.ico' style={{ width: '50px', marginRight: '20px' }} />
                    </Grid.Column>
                    <Grid.Column>
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Update Registry</b></p>
                    </Grid.Column>
                </div>
            </Grid.Row>
            <Grid.Row columns="1" >
                <Segment raised padded textAlign='left' className='form-container'>
                    <Form onSubmit={handleSubmit(onSubmit)} className="container">
                        <Form.Input fluid label='Participant Code' value={query.get("participant_code")} readOnly />
                        <Form.Field>
                            <label>JWT Token</label>
                            <input placeholder='Enter JWT Token' {...register("jwt_token", { required: true })} />
                        </Form.Field>
                        <Form.Field>
                            <label>Endpoint URL</label>
                            <input placeholder='Enter Endpoint URL' {...register("endpoint_url", { required: true })} />
                        </Form.Field>
                        <Form.Field>
                            <label>Encryption Cert Path</label>
                            <input placeholder='Enter Encryption Cert Path' {...register("encryption_cert_path", { required: true })} />
                        </Form.Field>
                        <Form.Field>
                            <label>Signing Cert Path</label>
                            <input placeholder='Enter Signing Cert Path' {...register("signing_cert_path", { required: true })} />
                        </Form.Field>
                        <Button disabled={sending} type='submit' className='primary center-element'>
                            {sending ? "Submitting" : "Submit"}
                        </Button>
                    </Form>
                </Segment>
            </Grid.Row>
        </Grid>
    </>

}