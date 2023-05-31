import { toast } from "react-toastify";


export const showError = (errorMsg) => {
    toast.error(errorMsg, {
      position: toast.POSITION.TOP_CENTER,
      autoClose: 2000
    });
}