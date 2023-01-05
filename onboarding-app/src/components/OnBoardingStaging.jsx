import { useState } from 'react'
import { Button, Form, Segment, Grid, Image, Radio } from 'semantic-ui-react'
import { sendData } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import * as _ from 'lodash'

export const OnBoardingStaging = () => {

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();

    const [sending, setSending] = useState(false)

    const onSubmit = (data) => {
        setSending(true)
        const formData = [{ "participant": { ...data, "roles": [data.roles] } }];
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
                        <Image src='favicon.ico' style={{ width: '50px', marginRight: '20px' }} />
                    </Grid.Column>
                    <Grid.Column>
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Onboarding - Staging</b></p>
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
                        <Button disabled={sending} type='submit' className='primary center-element'>
                            {sending ? "Submitting" : "Onboard"}
                        </Button>
                    </Form>
                </Segment>
            </Grid.Row>
        </Grid>
    </>

}