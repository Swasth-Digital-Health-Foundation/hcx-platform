import { useState } from 'react'
import { Button, Form, Segment, Grid, Image, Radio, Icon, Label } from 'semantic-ui-react'
import { sendData } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import { useQuery } from '../service/QueryService';
import { replaceString } from '../utils/StringUtil';
import * as _ from 'lodash'

export const OTPRegenerate = () => {

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();

    const [sending, setSending] = useState(false)

    let query = useQuery();

    const onSubmit = (data) => {
        setSending(true)
        const formData = { "primary_email": decodeURIComponent(query.get("primary_email")), "primary_mobile": decodeURIComponent(query.get("primary_mobile")) };
        sendData("/participant/otp/send", formData).then((data => {
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
                <div className='form-container banner'>
                    <Grid.Column>
                        <Image src='favicon.ico' style={{ width: '50px', marginRight: '20px' }} />
                    </Grid.Column>
                    <Grid.Column>
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX OTP Regenerate</b></p>
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
                                <br/><br/>
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