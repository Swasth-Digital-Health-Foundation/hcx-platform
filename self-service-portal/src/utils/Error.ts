import { ReactElement, JSXElementConstructor, ReactNode, ReactPortal } from "react";
import { ToastContentProps, toast } from "react-toastify";


export const showError = (errorMsg: string | number | boolean | ReactElement<any, string | JSXElementConstructor<any>> | Iterable<ReactNode> | ReactPortal | ((props: ToastContentProps<unknown>) => ReactNode) | null | undefined) => {
    toast.error(errorMsg, {
      position: toast.POSITION.TOP_CENTER,
      autoClose: 2000
    });
}