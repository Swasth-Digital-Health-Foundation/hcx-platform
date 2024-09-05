import { useState } from "react";
import { useForm } from "react-hook-form";
import { toast, ToastContainer } from "react-toastify";
import { Grid, Segment, Image, Message, Form, Button, Loader } from "semantic-ui-react";
import { get, getToken } from "../service/APIService";
import _ from 'lodash';
import { post } from "../utils/HttpUtil";

export const UpdateDetails = () => {

  const pcptApiVersion = process.env.REACT_APP_PARTICIPANT_API_VERSION;
  const hcxApiVersion = process.env.REACT_APP_HCX_API_VERSION;
  const baseUrl = process.env.REACT_APP_HCX_PATH;

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
    reset,
  } = useForm();
  const [sending, setSending] = useState(false);
  const [formErrors, setFormErrors] = useState({});
  const [token, setToken] = useState("");
  const [passwordType, setPasswordType] = useState("password")
  const [participantCode, setParticipantCode] = useState("");
  const [password, setPassword] = useState("");
  const [detailsUpdated, setDetailsUpdated] = useState(false);

  console.log(token)

  const togglePasswordView = () => {
    if (passwordType == 'password') {
      setPasswordType('text');
    } else {
      setPasswordType('password');
    }
  }

  const onSubmit = async (data) => {
    setSending(true)
    const headers = {"Authorization": "Bearer " + token}
    const formData = { "participant_code": participantCode, "endpoint_url": data.endpoint_url };
    post(`${baseUrl}/api/${hcxApiVersion}/participant/update`, formData, headers).then((data => {
      reset()
      setDetailsUpdated(true)
    })).catch(err => {
      toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
        position: toast.POSITION.TOP_CENTER
      });
    }).finally(() => {
      setSending(false)
    })
  };

  const getParticipant = (participantCode) => {
    setSending(true)
    return get(pcptApiVersion + "/participant/read/" + participantCode)
      .then((data => {
        console.log(_.get(data, 'data.participants')[0])
        return _.get(data, 'data.participants')[0];
      })).catch((function (err) {
        console.error(err)
        let errMsg = _.get(err, 'response.data.error.message') || "Internal Server Error"
        toast.error(errMsg, {
          position: toast.POSITION.TOP_CENTER
        });
      }))
      .finally(() => {
        setSending(false)
      })
  }

  const getAccessToken = async () => {
    setSending(true)
    setFormErrors({})
    let participant = await getParticipant(participantCode)
    let body = { "client_id": "registry-frontend", "username": participant.primary_email, "password": password, "grant_type": "password" }
    getToken("auth/realms/swasth-health-claim-exchange/protocol/openid-connect/token", body)
      .then((data => {
        console.log('data', data)
        setToken(_.get(data, 'data.access_token'))
      })).catch((err => {
        console.error(err);
        let errMsg = _.get(err, 'response.data.error_description')
        if (errMsg === 'Invalid user credentials') {
          setFormErrors({ password: errMsg });
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

  return (
    <>
      <ToastContainer autoClose={false} />
      <Grid centered>
        <Grid.Row columns="1">
          <div className="banner" style={{ width: "35%", marginTop: "30px" }}>
            <Grid.Column>
              <Image
                src="/images/logo.png"
                style={{ width: "50px", marginRight: "20px" }}
              />
            </Grid.Column>
            <Grid.Column>
              <p style={{ color: "white", fontSize: "30px" }}>
                <b>HCX Participant Update</b>
              </p>
            </Grid.Column>
          </div>
        </Grid.Row>
        {detailsUpdated ?
          <Grid.Row columns="1">
            <Segment raised padded style={{ width: '35%', height: '150%' }}>
              <Grid.Row columns="1">
                <div style={{ marginTop: '120px' }}>
                  <Grid.Column>
                    <p style={{ color: 'black', fontSize: '30px' }}><b>Updated successfully</b></p>
                  </Grid.Column>
                </div>
              </Grid.Row>
            </Segment>
          </Grid.Row>
          :
          <Grid.Row columns="1">
            <Segment style={{ width: "35%" }}>
              <Form
                disabled={sending}
                onSubmit={handleSubmit(onSubmit)}
                className="container"
                style={{ width: "60%", textAlign: "left" }}
              >
                {sending && <Loader active />}
                <Form.Field
                  disabled={sending}
                  className={{ error: "participant_code" in errors }}
                  required
                >
                  <label>Participant Code</label>
                  <input
                    className="input-text"
                    placeholder="Enter Participant Code"
                    onInput={e => setParticipantCode(e.target.value)}
                    {...register("participant_code", { required: true })}
                  />
                  {formErrors.participant_code && (<Grid.Row centered><div style={{ color: 'red', marginTop: '5px' }}>{formErrors.participant_code}</div></Grid.Row>)}
                </Form.Field>
                <Form.Field
                  disabled={sending}
                  className={{ error: "password" in errors || formErrors.password }}
                  required
                >
                  <label>Password</label>
                  <div class="ui icon input"><input type={passwordType} placeholder="Enter Password" onInput={e => setPassword(e.target.value)} {...register("password", { required: true })} /><i aria-hidden="true" class="eye link icon" onClick={() => togglePasswordView()}></i></div>
                </Form.Field>
                {token ? null :
                  <Button disabled={sending} onClick={e => {
                    e.preventDefault()
                    getAccessToken(e.target.value)
                  }} className="primary center-element button-color">
                    {sending ? "Verifying" : "Verify"}</Button>}
                {token ?
                  <Form.Field
                    disabled={sending}
                    className={{ error: "endpoint_url" in errors }}
                    required
                  >
                    <label>Endpoint Url</label>
                    <input
                      className="input-text"
                      placeholder="Enter Endpoint URL "
                      {...register("endpoint_url", { required: true })}
                    />
                  </Form.Field> : null}
                {token ?
                  <Button className={{ 'disabled': sending, 'primary center-element button-color': true }} type='submit'>
                    {sending ? "Submitting" : "Submit"}</Button>
                  : null}
              </Form>
            </Segment>
          </Grid.Row>
        }
      </Grid>
    </>
  );
};
