import { useEffect, useRef, useState } from "react";
import { useForm } from "react-hook-form";
import { toast, ToastContainer } from "react-toastify";
import { Grid, Segment, Image, Message, Form, Button, Loader } from "semantic-ui-react";
import _ from 'lodash';
import { useQuery } from "../service/QueryService";
import { post } from "../service/APIService";

export const CommunicationVerify = () => {

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
    reset,
  } = useForm();

  const query = useQuery();
  const [sending, setSending] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [isFailed, setIsFailed] = useState(false);
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [participantName, setParticipantName] = useState("");
  const windowWidth = useRef(window.innerWidth);
  const windowHeight = useRef(window.innerHeight);
  const [smallScreen, setSmallScreen] = useState(false)
  const [emailVerified, setEmailVerified] = useState(false)
  const [phoneVerified, setPhoneVerified] = useState(false)

  useEffect(() => {
    if (windowWidth.current < 450 || windowHeight < 650) {
      setSmallScreen(true)
    }

    setParticipantName(getPayload(query.get('jwt_token')) != null ? getPayload(query.get('jwt_token')).participant_name : "");

    (async () => {
      let participantCode = getPayload(query.get('jwt_token')).participant_code
      const reqBody = { filters: { participant_code: { 'eq': participantCode } } };
      await post("/applicant/search/?fields=communication,sponsors", reqBody)
        .then((async function (data) {
          let participant = _.get(data, 'data.participants')[0] || {}
          if (participant) {
            setEmailVerified(_.get(participant, 'communication.emailVerified') || false)
            setPhoneVerified(_.get(participant, 'communication.phoneVerified') || false)
          }
        })).catch((function (err) {
          console.error(err)
          toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
            position: toast.POSITION.TOP_CENTER
          });
        }))
    })()

    if (query.get('email') != null) {
      setEmail(query.get('email'))
    } else if (query.get('phone') != null) {
      setPhone(query.get('phone'))
    }
  }, []);

  const onSubmit = (data) => {
    setSending(true)
    const body = { "jwt_token": query.get("jwt_token"), "status": data.status, "comments": data.comments };
    console.log(body)
    post("/applicant/verify", body).then((response => {
      setSubmitted(true)
    })).catch(err => {
      showError(_.get(err, 'response.data.error.message') || "Internal Server Error");
    }).finally(() => {
      setSending(false)
    })
  }

  const getPayload = (token) => {
    try {
      const decoded = token.split(".");
      const payload = JSON.parse(atob(decoded[1]));
      console.log('payload: ', payload)
      return payload;
    } catch (err) {
      console.error(err)
      showError("Invalid JWT Token ", err)
    }
  }

  const showError = (errorMsg) => {
    toast.error(errorMsg, {
      position: toast.POSITION.TOP_CENTER,
      autoClose: 2000
    });
  }

  const getMessageLayout = (message) => {
    return (
      <Segment raised padded style={{ width: smallScreen ? '80vw' : '30vw', height: '150%' }}>
        <Grid.Row columns="1">
          <div style={{ marginTop: '100px' }}>
            <Grid.Column>
              <p style={{ color: 'black', fontSize: '30px' }}><b>{message}</b></p>
            </Grid.Column>
          </div>
        </Grid.Row>
      </Segment>
    );
  }

  return (
    <>
      <ToastContainer autoClose={false} />
      <Grid centered>
        <Grid.Row>
          <div className="banner" style={{ width: smallScreen ? '80vw' : '30vw', marginTop: "30px" }}>
            <Grid.Column>
              <Image
                src="/images/logo.png"
                style={{ width: smallScreen ? '10vw' : '4vw', marginLeft: "30px" }}
              />
            </Grid.Column>
            <Grid.Column>
              <p style={{ color: "white", fontSize: smallScreen ? '1.2rem' : '1.8rem' }}>
                <b>Communication Details Verification</b>
              </p>
            </Grid.Column>
          </div>
        </Grid.Row>
        {(email && emailVerified === true) || (phone && phoneVerified === true) ?
          <Grid.Row>
            {getMessageLayout('Link is Expired')}
          </Grid.Row> :
          <Grid.Row>
            {submitted ?
              <Grid.Row>
                {getMessageLayout('Submitted Successfully')}
              </Grid.Row>
              :
              <Grid.Row >
                <Segment style={{ width: smallScreen ? '80vw' : '30vw' }}>
                  <Form onSubmit={handleSubmit(onSubmit)} >
                    <div style={{ textAlign: 'left', marginLeft: smallScreen ? '0.8rem' : '5rem' }}>
                      {participantName ?
                        <Form.Field>
                          <b>Participant Name:</b>&ensp;{participantName}
                        </Form.Field> : null}
                      {phone ?
                        <Form.Field>
                          <b>Phone Number:</b>&ensp;{phone}
                        </Form.Field> : null}
                      {phone ?
                        <Form.Field>
                          <b>Do you want to verify your mobile number?</b>
                        </Form.Field> : null}
                      {email ?
                        <Form.Field>
                          <b>Primary Email:</b>&ensp;{email}
                        </Form.Field> : null}
                      {email ?
                        <Form.Field>
                          <b>Do you want to verify your email address?</b>
                        </Form.Field> : null}
                      <Grid style={{ textAlign: 'center', marginTop: "10px", marginBottom: "10px", justifyContents: 'space-between', display: 'flex', marginLeft: '5rem' }}>
                        <Form.Field style={{ textAlign: 'center' }} disabled={sending}>
                          <input
                            id="successful"
                            type="radio"
                            label='successful'
                            name='status'
                            value='successful'
                            onClick={e => { setIsFailed(false) }}
                            {...register("status", { required: true })}
                          /> Yes
                        </Form.Field>
                        <Form.Field style={{ textAlign: 'center' }} disabled={sending}>
                          <input
                            id="failed"
                            type="radio"
                            label='failed'
                            name='status'
                            value='failed'
                            onClick={e => { setIsFailed(true) }}
                            {...register("status", { required: true })}
                          /> No
                        </Form.Field>
                      </Grid>
                      {isFailed ?
                        <Form.Field>
                          <textarea rows="2" placeholder='Enter reason..' style={{ width: '80%', marginBottom: '20px' }} {...register("comments")}></textarea>
                        </Form.Field> : null}
                    </div>
                    <Button className={{ 'disabled': sending, 'primary center-element button-color': true }} type='submit'>
                      {sending ? "Submitting" : "Submit"}</Button>
                  </Form>
                </Segment>
              </Grid.Row>
            }
          </Grid.Row>
        }
      </Grid>
    </>
  );
};
