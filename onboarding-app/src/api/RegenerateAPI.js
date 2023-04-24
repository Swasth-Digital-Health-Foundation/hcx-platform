import { toast } from "react-toastify";
import { post } from "../service/APIService";
import * as _ from 'lodash';

export const regenerateLink = (formState) => {
    const formData = { "participant_code": _.get(formState, 'participant_code'), "participant_name": _.get(formState, 'participant.participant_name'), "primary_email": _.get(formState, 'participant.primary_email'), "primary_mobile": _.get(formState, 'participant.primary_mobile') };
    post("/participant/verification/link/send", formData).then((data => {
        toast.success("Links are regenerated successfully.", {
            position: toast.POSITION.TOP_CENTER, autoClose: 2000
        });
    })).catch(err => {
        toast.error(_.get(err, 'response.data.error.message') || "Internal Server Error", {
            position: toast.POSITION.TOP_CENTER, autoClose: 2000
        });
    })
}