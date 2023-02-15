import { Button, Form, Segment, Grid, Image, Loader, Message } from 'semantic-ui-react'
import { ToastContainer } from 'react-toastify';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { generateJWS } from '../utils/JWTUtil';
import { useHistory } from 'react-router-dom/cjs/react-router-dom.min';
import { useDispatch } from 'react-redux';
import { updateForm } from '../store/store';

export const PayorSystem = () => {

    const { register, handleSubmit, formState: { errors } } = useForm();
    const [sending, setSending] = useState(false)
    const [redirect, setRedirect] = useState(false)
    const dispatch = useDispatch()
    const history = useHistory();

    const onSubmit = (data) => {
        generateJWS("swasthmockpayorjwt.gmail@swasth-hcx-staging", data.participant_name).then(token => {
            dispatch(updateForm({ payor_system: { participant_name: data.participant_name, primary_email: data.primary_email, primary_mobile: data.primary_mobile } }))
            setRedirect(true)
            setTimeout(function () {
                history.push("/onboarding/process" + "?jwt_token=" + token);
            }.bind(this), 3000);
        }).catch(error => {
            console.log(error)
        })
    }

    return <>
        <ToastContainer autoClose={false} />
        {redirect ? null :
            <Grid centered>
                <Grid.Row columns="1">
                    <div className='payor-system-banner' style={{ width: '35%', marginTop: '30px' }}>
                        <Grid.Column>
                            <p style={{ color: 'white', fontSize: '30px' }}><b>Mock Payor System </b><br /><div style={{ fontSize: '25px' }}>Onboarding to HCX</div></p>
                        </Grid.Column>
                    </div>
                </Grid.Row>
                <Segment raised padded style={{ width: '35%' }}>
                    {sending && <Loader active />}
                    <Form disabled={sending} onSubmit={handleSubmit(onSubmit)}>
                        <Message>
                            <Message.Content>Please enter the below details to verify and onboard to HCX</Message.Content>
                        </Message>
                        <Grid centered textAlign='left'>
                            <Grid.Row>
                                <Form.Field disabled={sending} className={{ 'error': 'participant_name' in errors, 'form-input-field': true }} required>
                                    <label>Organisation Name</label>
                                    <input placeholder='Enter Organisation Name' {...register("participant_name", { required: true })} />
                                </Form.Field>
                            </Grid.Row>
                            <Grid.Row>
                                <Form.Field disabled={sending} className={{ 'error': 'primary_email' in errors, 'form-input-field': true }} required>
                                    <label>Email</label>
                                    <input placeholder='Enter Email' {...register("primary_email", { required: true })} />
                                </Form.Field>
                            </Grid.Row>
                            <Grid.Row>
                                <Form.Field disabled={sending} className={{ 'error': 'primary_mobile' in errors, 'form-input-field': true }} required>
                                    <label>Phone Number</label>
                                    <input placeholder='Enter Phone Number' {...register("primary_mobile", { required: true })} />
                                </Form.Field>
                            </Grid.Row>
                            <Grid.Row>
                                <Button className={{ 'primary center-element payor-system-button': true }} disabled={sending} type='submit'>
                                    {sending ? "Submitting" : "Submit"}
                                </Button>
                            </Grid.Row>
                        </Grid>
                    </Form>
                </Segment>
            </Grid>}
        {redirect ?
            <Grid centered>
                {redirect && <Loader active />}
                <Grid.Row columns="1">
                    <Segment raised padded style={{ width: '35%', height: '150%', marginTop: '70px'}}>
                        <Grid.Row columns="1">
                            <div style={{ marginTop: '130px' }}>
                                <Grid.Column>
                                    <p style={{ color: 'black', fontSize: '30px' }}><b>Redirecting to HCX Onboarding</b></p>
                                </Grid.Column>
                            </div>
                        </Grid.Row>
                    </Segment>
                </Grid.Row>
            </Grid> : null}
    </>

}