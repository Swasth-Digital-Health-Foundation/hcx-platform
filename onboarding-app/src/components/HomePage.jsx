import { useState, } from 'react'
import { Button, Form, Segment, Grid, Image, Loader, Message } from 'semantic-ui-react'
import { get } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import * as _ from 'lodash'
import { useHistory } from "react-router-dom";
import { useDispatch, useSelector } from 'react-redux';
import { updateForm } from '../store/store';
import { generateToken, isPasswordSet } from '../service/KeycloakService';

export const Home = () => {

    const dispatch = useDispatch()
    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();
    const [existingUser, setExistingUser] = useState(false)
    const [loader, setLoader] = useState(false)
    const [participantCode, setParticipantCode] = useState(false)
    let history = useHistory();
    let state = 0;
    const formStore = useSelector((state) => state)

    const apiVersion = process.env.REACT_APP_PARTICIPANT_API_VERSION;

    const getParticipantDetails =  () => {
        setLoader(true)
        get(apiVersion + "/participant/read/" + participantCode + "?fields=verificationStatus,sponsors")
            .then(( function (data) {
                let participant = _.get(data, 'data.participants')[0] || {}
                // if (await isPasswordSet(participant.osOwner[0])) {
                //     if (participant.verificationStatus.status === 'successful') {
                //         state = 3
                //     } else {
                //         state = 2
                //     }
                // } else 
                console.log(participant)
                if (participant.verificationstatus.status === 'successful') {
                    state = 2;
                } else {
                    state = 1;
                }
                dispatch(updateForm({ participant: participant, participant_code: participantCode, identity_verification: participant.sponsors[0] ? participant.sponsors[0].status : 'pending' }))
                if (participant.status === 'Active') {
                    history.push("/onboarded");
                } else {
                    history.push("/onboarding/process" + "?state=" + state);
                }
            })).catch((function (err) {
                console.error(err)
                let errMsg = _.get(err, 'response.data.error.message')
                toast.error(errMsg || "Internal Server Error", {
                    position: toast.POSITION.TOP_CENTER
                });
            }))
            .finally(() => {
                setLoader(false)
            })
    }

    const newUser = () => {
        history.push("/onboarding/process");
    }

    return <>
        <ToastContainer autoClose={false} />
        <Grid centered>
            <Grid.Row columns="1">
                <div className='banner' style={{ width: '45%', marginTop: '30px' }}>
                    <Grid.Column>
                        <Image src='favicon.ico' style={{ width: '50px', marginRight: '20px' }} />
                    </Grid.Column>
                    <Grid.Column>
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Onboarding</b></p>
                    </Grid.Column>
                </div>
            </Grid.Row>
            <Grid.Row columns="1">
                <Segment raised padded style={{ width: '45%' }}>
                    {loader && <Loader active />}
                    <Message>
                        <Message.Header>Welcome to HCX Onboarding!</Message.Header><br/>
                        <Message.Content style={{ textAlign:'left' }}>Following is the onboarding process for new and existing users:</Message.Content>
                        <Message.Content style={{ textAlign:'left' }}><b>New User:</b> There are 4 steps in onboarding process:</Message.Content>
                        <Message.List>
                            <Message.Item><b>Basic Details</b></Message.Item>
                            <Message.Item><b>OTP Verification</b></Message.Item>
                            <Message.Item><b>Set Password</b></Message.Item>
                            <Message.Item><b>Update Complete Details</b></Message.Item>
                        </Message.List><br/>
                        <Message.Content style={{ textAlign:'left' }}><b>Existing User:</b> If you have started onboarding process and exited the form before completion. Please select <b>existing user</b> and enter your <b>participant code</b>. Form will take you to the stage from where you have exited.</Message.Content>
                    </Message>
                    <Form>
                        <Grid centered>
                            {existingUser ? null :
                                <Grid.Row>
                                    <button style={{ width: '36%' }} floated='right' class="ui primary button button-color" onClick={newUser}>New User</button>
                                </Grid.Row>}
                            {existingUser ? null :
                                <Grid.Row>
                                    <button style={{ width: '36%' }} class="ui primary button button-color" onClick={e => setExistingUser(true)}> Existing User</button>
                                </Grid.Row>}
                            {existingUser ?
                                <Grid.Row>
                                    <Form.Field style={{ width: '40%' }} disabled={loader}>
                                        <input placeholder='Enter Participant Code' onInput={e => setParticipantCode(e.target.value)} {...register("participant_code", { required: true })} />
                                    </Form.Field>
                                </Grid.Row> : null}
                            {existingUser ?
                                <Grid.Row>
                                    <Button disabled={loader} onClick={getParticipantDetails} className="primary center-element button-color">
                                        {loader ? "Submitting" : "Submit"}</Button>
                                </Grid.Row> : null}
                        </Grid>
                    </Form>
                </Segment>
            </Grid.Row>

        </Grid>
    </>

}