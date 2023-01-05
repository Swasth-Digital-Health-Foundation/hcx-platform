import { useState } from 'react'
import { Button, Form, Segment, Grid, Image, Radio } from 'semantic-ui-react'
import { sendData } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import { useQuery } from '../service/QueryService';
import * as _ from 'lodash';

export const OTPVerify = () => {

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();

    const [sending, setSending] = useState(false)

    let query = useQuery();

    const onSubmit = (data) => {
        setSending(true)
        const formData = [{ "primary_email": query.get("primary_email"), "otp": data.email_otp }, { "primary_mobile": query.get("primary_email"), "otp": data.phone_otp }];
        sendData("/participant/verify", formData).then((data => {
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
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX OTP Verification</b></p>
                    </Grid.Column>
                </div>
            </Grid.Row>
            <Grid.Row columns="1" >
                <Segment raised padded textAlign='left' className='form-container'>
                    <Form onSubmit={handleSubmit(onSubmit)} className="container">
                        <Form.Input fluid label='Email' value={query.get("primary_email")} readOnly />
                        <Form.Input fluid label='Phone Number' value={query.get("primary_mobile")} readOnly />
                        <Form.Field>
                            <label>Email OTP</label>
                            <input placeholder='Enter Email OTP' {...register("email_otp", { required: true })} />
                        </Form.Field>
                        <Form.Field>
                            <label>Phone OTP</label>
                            <input placeholder='Enter Phone OTP' {...register("phone_otp", { required: true })} />
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