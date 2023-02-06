import { isPasswordSet, setPassword } from "../service/KeycloakService";
import { useEffect, useState } from 'react'
import { Button, Form, Grid, Loader, Segment, TextArea, Visibility } from 'semantic-ui-react'
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import * as _ from 'lodash';
import { useSelector } from 'react-redux';
import { get } from "../service/APIService";
import { useHistory } from "react-router-dom/cjs/react-router-dom.min";


export const SetPassword = ({ tab, changeTab, formState, setState }) => {

    const apiVersion = process.env.REACT_APP_PARTICIPANT_API_VERSION;

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();
    const [sending, setSending] = useState(false)
    const [osOwner, setOsOwner] = useState("")
    const [isPasswordUpdated, setIsPasswordUpdated] = useState(false)
    const formStore = useSelector((state) => state)

    useEffect(() => {
        if (_.get(formState, 'participant') == null) {
            setState({ ...formState, ...formStore.formState })
        }

        (async () => {
            await get(apiVersion + "/participant/read/" + formStore.formState.participant_code)
                .then((async function (data) {
                    let participant = _.get(data, 'data.participants')[0] || {}
                    if (participant) {
                        if (await isPasswordSet(participant.osOwner[0])) {
                            setIsPasswordUpdated(true);
                        }
                        setOsOwner(participant.osOwner[0]);
                    }
                })).catch((function (err) {
                    console.error(err)
                    toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                        position: toast.POSITION.TOP_CENTER
                    });
                }))
        })()
    }, [tab]);

    const onSubmit = (data) => {
        setSending(true)
        if (data.password === data.re_password) {
            setPassword(osOwner, data.password).then((data => {
                toast.success("Form is submitted successfully", {
                    position: toast.POSITION.TOP_CENTER, autoClose: 2000
                });
                reset()
                changeTab(3)
            })).catch(err => {
                toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                    position: toast.POSITION.TOP_CENTER
                });
            }).finally(() => {
                setSending(false)
            })
        } else {
            toast.error("Incorrect password", {
                position: toast.POSITION.TOP_CENTER
            });
        }
    }

    const nextPage = () => {
        changeTab(3)
    }

    return <>
        <ToastContainer autoClose={false} />
        {isPasswordUpdated ?
            <div className='form-main' style={{ marginTop: '15px' }}>
                <Grid textAlign="center">
                    <Grid.Row>
                        <p>Password is already set, click Next.</p>
                    </Grid.Row>
                    <Grid.Row>
                        <Button disabled={sending} onClick={e => {
                            e.preventDefault()
                            nextPage()
                        }} className="primary center-element button-color">
                            Next</Button>
                    </Grid.Row>
                </Grid>
            </div> :
            <Form onSubmit={handleSubmit(onSubmit)} className="container">
                {sending && <Loader active />}
                <div className='form-main' style={{ marginTop: '15px' }}>
                    <Form.Field disabled={sending} className={{ 'error': 'password' in errors }} required>
                        <input className='input-text' type='password' placeholder='Enter Password' {...register("password", { required: true })} />
                    </Form.Field>
                    <Form.Field disabled={sending} className={{ 'error': 'password' in errors }} required>
                        <input className='input-text' type='password' placeholder='Re Enter Password' {...register("re_password", { required: true })} />
                    </Form.Field>
                </div><br />
                <Button disabled={sending} type="submit" className="primary center-element button-color">
                    {sending ? "Submitting" : "Submit"}</Button>
            </Form>
        }
    </>


}