import { Html5QrcodeScanner } from "html5-qrcode";
import { useState } from "react";

const qrcodeRegionId = "html5qr-code-full-region";

// Creates the configuration object for Html5QrcodeScanner.
const createConfig = (props: any) => {
  let config: any = {};
  if (props.fps) {
    config.fps = props.fps;
  }
  if (props.qrbox) {
    config.qrbox = props.qrbox;
  }
  if (props.aspectRatio) {
    config.aspectRatio = props.aspectRatio;
  }
  if (props.disableFlip !== undefined) {
    config.disableFlip = props.disableFlip;
  }
  return config;
};

const Html5QrcodePlugin = (props: any) => {
  const [initialized, setInitialized] = useState(props.setInitialized);
  const [html5QrcodeScanner, setHtml5QrcodeScanner] = useState<any>(null);
  const [scan, setScan] = useState(false)

  const initializeScanner = async () => {
    try {
      const config = createConfig(props);
      const verbose = props.verbose === true;
      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      const scanner = new Html5QrcodeScanner(qrcodeRegionId, config, verbose);
      scanner.render(props.qrCodeSuccessCallback, props.qrCodeErrorCallback);
      setHtml5QrcodeScanner(scanner);
      setInitialized(true);
    } catch (error) {
      console.error("Failed to initialize the QR code scanner: ", error);
    }
  };

  const startScanner = () => {
    if (initialized) {
      initializeScanner();
      setScan(!scan)
    }
  };

  // const stopScanner = () => {
  //   if (html5QrcodeScanner) {
  //     html5QrcodeScanner.clear().catch((error: any) => {
  //       console.error("Failed to clear html5QrcodeScanner. ", error);
  //     });
  //   }
  // };

  return (
    <>
      <div className="flex justify-center gap-6 text-center">
        <button>Scan provider QR code to capture patient details</button>
        {/* <CustomButton disabled={false} onClick={startScanner} text={"Scan"} className="py-2 px-1 w-20"/> */}
      </div>
      <button
        disabled={false}
        onClick={startScanner}
        className={`align-center m-auto mb-2 mt-1 flex w-auto px-5 h-10 justify-center rounded pt-1.5 font-medium text-gray
            cursor-pointer bg-primary text-white`}
      >
        {!scan ? "scan" : "stop scanning"}
      </button>
      <div id={qrcodeRegionId} className={scan ? "block" : "hidden"} />
    </>
  );
};

export default Html5QrcodePlugin;
