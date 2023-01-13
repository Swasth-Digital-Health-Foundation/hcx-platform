import { useState } from 'react'
import { Button, Form, Segment, Grid, Image, Radio } from 'semantic-ui-react'
import { sendData } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import { useQuery } from '../service/QueryService';
import { replaceString } from '../utils/StringUtil';
import * as _ from 'lodash';

export const OTPVerify = () => {

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();

    const [sending, setSending] = useState(false)

    let query = useQuery();

    const onSubmit = (data) => {
        setSending(true)
        const formData = [{ "primary_email": decodeURIComponent(query.get("primary_email")), "otp": data.email_otp }, { "primary_mobile": decodeURIComponent(query.get("primary_email")), "otp": data.phone_otp }];
        sendData("/participant/verify", formData).then((data => {
            toast.success("Form is submitted successfully", {
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
            <Grid centered container>
                <Grid.Row columns="1">
                    <div className='banner'>
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
                            <Grid columns='equal'>
                                <Grid.Row columns={2}>
                                    <Grid.Column>
                                        <div class="user-details"><i class="envelope icon" style={{ fontSize: '1.5em' }}></i>{replaceString(decodeURIComponent(query.get("primary_email")), 5, "X")}</div>
                                    </Grid.Column>
                                    <Grid.Column>
                                        <div class="user-details"><i class="phone icon" style={{ fontSize: '1.5em' }}></i>{replaceString(decodeURIComponent(query.get("primary_mobile")), 5, "X")}</div>
                                    </Grid.Column>
                                </Grid.Row>
                                <Grid.Row>
                                    <Grid.Column>
                                        <div class="user-details">{decodeURIComponent(query.get("identity_verified")) === "accepted" ? <i class="check circle icon" style={{ fontSize: '1.5em', color: 'green' }} ></i> : <i class="times circle icon" style={{ fontSize: '1.5em', color: 'red' }} ></i>}Identity verified</div>
                                    </Grid.Column>
                                </Grid.Row>
                                <Grid.Row style={{ width: "70px" }}>
                                    <Grid.Column>
                                        <Form.Field className={{ 'error': 'email_otp' in errors }} required>
                                            <label>Email OTP</label>
                                            <input placeholder='Enter Email OTP' {...register("email_otp", { required: true })} />
                                        </Form.Field>
                                    </Grid.Column>
                                    <Grid.Column>
                                        <Form.Field className={{ 'error': 'phone_otp' in errors }} required>
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
                            </Grid>
                        </Form>
                    </Segment>
                </Grid.Row>
            </Grid>
    </>


}