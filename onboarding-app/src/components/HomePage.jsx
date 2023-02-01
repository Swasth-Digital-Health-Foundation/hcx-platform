import { useState, } from 'react'
import { Button, Form, Segment, Grid, Image, Loader } from 'semantic-ui-react'
import { get } from '../service/APIService';
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import * as _ from 'lodash'
import { useHistory } from "react-router-dom";
import { useDispatch } from 'react-redux';
import { updateForm } from '../store/store';

export const Home = () => {

    const dispatch = useDispatch()
    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();
    const [existingUser, setExistingUser] = useState(false)
    const [loader, setLoader] = useState(false)
    const [participantCode, setParticipantCode] = useState(false)
    let history = useHistory();
    let state = 0;

    const apiVersion = process.env.REACT_APP_PARTICIPANT_API_VERSION;

    const getParticipantDetails = () => {
        setLoader(true)
        get(apiVersion + "/participant/read/" + participantCode + "?fields=verificationStatus,sponsors")
            .then((data => {
                let participant = _.get(data, 'data.participants')[0] || {}
                if (participant.verificationStatus.status === 'successful') {
                    state = 2;
                } else {
                    state = 1;
                }
                dispatch(updateForm({ participant: participant, participant_code: participantCode, identity_verification: participant.sponsors[0].status }))
                if (participant.status === 'Active') {
                    history.push("/onboarded");
                } else {
                    history.push("/onboarding/process" + "?state=" + state);
                }
            })).catch((err => {
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
                <div className='banner' style={{ width: '35%', marginTop: '30px' }}>
                    <Grid.Column>
                        <Image src='favicon.ico' style={{ width: '50px', marginRight: '20px' }} />
                    </Grid.Column>
                    <Grid.Column>
                        <p style={{ color: 'white', fontSize: '30px' }}><b>HCX Onboarding</b></p>
                    </Grid.Column>
                </div>
            </Grid.Row>
            <Grid.Row columns="1">
                <Segment raised padded style={{ width: '35%' }}>
                    {loader && <Loader active />}
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