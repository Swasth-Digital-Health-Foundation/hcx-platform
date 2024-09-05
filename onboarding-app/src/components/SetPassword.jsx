import { isPasswordSet, setPassword } from "../service/KeycloakService";
import { useEffect, useState } from 'react'
import { Button, Divider, Form, Grid, Header, Icon, Label, Loader, Message } from 'semantic-ui-react'
import { useForm } from "react-hook-form";
import { ToastContainer, toast } from 'react-toastify';
import * as _ from 'lodash';
import { useSelector } from 'react-redux';
import { get, getToken, post } from "../service/APIService";

export const SetPassword = ({ changeTab, formState, setState }) => {

    const apiVersion = process.env.REACT_APP_PARTICIPANT_API_VERSION;

    const { register, handleSubmit, watch, formState: { errors }, reset } = useForm();
    const [sending, setSending] = useState(false)
    const [osOwner, setOsOwner] = useState("")
    const [isPasswordUpdated, setIsPasswordUpdated] = useState(false)
    const formStore = useSelector((state) => state)
    const [passwordType, setPasswordType] = useState("password")
    const [formErrors, setFormErrors] = useState({});
    const [identityVerification, setIdentityVerfication] = useState('pending');
    const [communicationVerification, setCommunicationVerfication] = useState('pending');
    const [emailVerified, setEmailVerified] = useState('pending')
    const [phoneVerified, setPhoneVerified] = useState('pending')

    const togglePasswordView = () => {
        if (passwordType == 'password') {
            setPasswordType('text');
        } else {
            setPasswordType('password');
        }
    }

    useEffect(() => {

        if (_.get(formState, 'participant') == null) {
            setState({ ...formState, ...formStore.formState })
        }

        (async () => {
            let participantCode = _.get(formState, 'participant_code') || formStore.formState.participant_code
            const reqBody = { filters: { participant_code: { 'eq': participantCode } } };
            await post("/applicant/search?fields=communication,sponsors", reqBody)
                .then((async function (data) {
                    let participant = _.get(data, 'data.participants')[0] || {}
                    if (participant) {
                        if (await isPasswordSet(participant.osOwner[0])) {
                            setIsPasswordUpdated(true);
                        }
                        setOsOwner(participant.osOwner[0]);
                        setIdentityVerfication(_.get(participant, 'sponsors') ? participant.sponsors[0].status : 'pending')
                        setCommunicationVerfication(_.get(participant, 'communication.status') || "pending")
                        setEmailStatus(_.get(participant, 'communication.status'), _.get(participant, 'communication.emailVerified'));
                        setPhoneStatus(_.get(participant, 'communication.status'), _.get(participant, 'communication.phoneVerified'));
                    }
                })).catch((function (err) {
                    console.error(err)
                    toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                        position: toast.POSITION.TOP_CENTER
                    });
                }))
        })()
    }, [formState]);

    const onSubmit = async (data) => {
        setSending(true)
        if (isPasswordUpdated) {
            changeFlow(data.password)
        } else {
            if (data.password === data.confirm_password) {
                setPassword(osOwner, data.password).then((async function () {
                    changeFlow(data.password)
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
    }

    const changeFlow = async (password) => {
        const token = await getAccessToken(password)
        if (token !== "" && _.keys(formErrors).length === 0) {
            setState(prevState => ({ ...formState, ...{ access_token: token } }))
            changeTab(2)
        }
    }

    const getAccessToken = (password) => {
        setSending(true)
        setFormErrors({})
        let body = { "client_id": "registry-frontend", "username": _.get(formState, 'participant.primary_email'), "password": password, "grant_type": "password" }
        return getToken("auth/realms/swasth-health-claim-exchange/protocol/openid-connect/token", body)
            .then((data => {
                return _.get(data, 'data.access_token');
            })).catch((err => {
                console.error(err);
                let errMsg = _.get(err, 'response.data.error_description')
                if (errMsg === 'Invalid user credentials') {
                    setFormErrors({ passwordIncorrect: errMsg });
                } else {
                    toast.error(errMsg || "Internal Server Error", {
                        position: toast.POSITION.TOP_CENTER
                    });
                }
            }))
            .finally(() => {
                setSending(false)
            })
    }

    const regenerate = () => {
        setSending(true);
        const formData = { "participant_code": _.get(formState, 'participant_code'), "participant_name": _.get(formState, 'participant.participant_name'), "primary_email": _.get(formState, 'participant.primary_email'), "primary_mobile": _.get(formState, 'participant.primary_mobile') };
        post("/participant/verification/link/send", formData).then((data => {
            toast.success("Links are regenerated successfully.", {
                position: toast.POSITION.TOP_CENTER, autoClose: 2000
            });
        })).catch(err => {
            toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                position: toast.POSITION.TOP_CENTER, autoClose: 2000
            });
        }).finally(() => {
            setSending(false);
        })
    }

    const getVerificationStatus = () => {
        setSending(true)
        const reqBody = { filters: { participant_code: { 'eq': _.get(formState, 'participant_code') } } };
        post("/applicant/search?fields=communication,sponsors", reqBody)
            .then((function (data) {
                const participant = _.get(data, 'data.participants')[0] || {}
                setIdentityVerfication(_.get(participant, 'sponsors') ? participant.sponsors[0].status : 'pending')
                setCommunicationVerfication(_.get(participant, 'communication.status') || "pending")
                setEmailStatus(_.get(participant, 'communication.status'), _.get(participant, 'communication.emailVerified'));
                setPhoneStatus(_.get(participant, 'communication.status'), _.get(participant, 'communication.phoneVerified'));
            })).catch((function (err) {
                console.error(err)
                toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
                    position: toast.POSITION.TOP_CENTER
                });
            }))
            .finally(() => {
                setSending(false)
            })
    }

    const setPhoneStatus = (commStatus, phoneStatus) => {
        if (phoneStatus === null) {
            setPhoneStatus('disabled')
        } else if (commStatus === 'failed' && phoneStatus === false){
            setPhoneVerified('failed')
        } else if (phoneStatus === true) {
            setPhoneVerified('successful')
        }
    }

    const setEmailStatus = (commStatus, emailStatus) => {
        if (emailStatus === null) {
            setEmailStatus('disabled')
        }else if(commStatus === 'failed' && emailStatus === false){
            setEmailVerified('failed')
        } else if (emailStatus === true) {
            setEmailVerified('successful')
        }
    }

    const getIdentityVerification = () => {
        if (identityVerification === 'accepted') {
            return <Label color='green' horizontal> Accepted </Label>;
        } else if (identityVerification === 'rejected') {
            return <Label color='red' horizontal> Rejected </Label>;
        } else if (identityVerification === 'pending') {
            return <Label color='yellow' horizontal> Pending </Label>;
        } else {
            return null;
        }
    }

    const getStatus = (type) => {
        if (type === 'successful') {
            return <Label color='green' horizontal> Successful </Label>;
        } else if (type === 'failed') {
            return <Label color='red' horizontal> Failed </Label>;
        } else if (type === 'pending') {
            return <Label color='yellow' horizontal> Pending </Label>;
        } else {
            return null;
        }
    }

    return <>
        <ToastContainer autoClose={false} />
        <Form disabled={sending} onSubmit={handleSubmit(onSubmit)} className="container">
            {sending && <Loader active />}
            {isPasswordUpdated ? null :
                <Message disabled={sending}>
                    <Message.Content style={{ textAlign: 'left' }}>Password should be minimum 8 characters and should contain 1 Uppercase Character, 1 Lowercase Character, 1 Special Character and 1 Number. 	</Message.Content>
                </Message>
            }
            <div className='form-main' style={{ marginTop: '15px' }}>
                <Divider horizontal>
                    <Header as='h4'>
                        Verification
                    </Header>
                </Divider>
                <Form.Field>
                    <b>Identity:</b>&ensp;
                    {getIdentityVerification()}
                    <b>Email:</b>&ensp;
                    {getStatus(emailVerified)}
                    <b>Phone:</b>&ensp;
                    {getStatus(phoneVerified)}
                </Form.Field>
                <Divider horizontal>
                    <Header as='h4'>
                        {isPasswordUpdated  ? 'Verify Password' : 'Set Password'}
                    </Header>
                </Divider>
                <Form.Field disabled={sending} className={{ 'error': 'password' in errors || formErrors.passwordIncorrect }} required>
                    <div class="ui icon input"><input type={passwordType} placeholder={isPasswordUpdated ? "Enter Password" : "Set Password"} {...register("password", { required: true, pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*])[a-zA-Z\d!@#$%^&*]{8,}$/ })} /><i aria-hidden="true" class="eye link icon" onClick={() => togglePasswordView()}></i></div>
                </Form.Field>
                {isPasswordUpdated ? null :
                    <Form.Field disabled={sending} className={{ 'error': 'confirm_password' in errors }} required>
                        <div class="ui icon input"><input type={passwordType} placeholder="Confirm Password" {...register("confirm_password", { required: true, pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*])[a-zA-Z\d!@#$%^&*]{8,}$/ })} /><i aria-hidden="true" class="eye link icon" onClick={() => togglePasswordView()}></i></div>
                    </Form.Field>
                }
            </div><br />
            <Button className={{ 'disabled': sending, 'primary center-element button-color': true }} type='submit'>
                {isPasswordUpdated ? "Verify" : "Submit"}</Button>
            {communicationVerification === 'successful' && identityVerification === 'accepted' ? null :
                <Grid.Column style={{ textAlign: 'left', marginBottom: communicationVerification === 'successful' ? '0px' : '-22px' }}>
                    <a disabled={sending} onClick={getVerificationStatus} href='#'> Refresh</a>
                </Grid.Column>
            }
            {communicationVerification === 'successful' ? null :
                <Grid.Column style={{ textAlign: 'right' }}>
                    <a disabled={sending} onClick={regenerate} href='#'> Regenerate Link</a>
                </Grid.Column>
            }
        </Form>
    </>


}