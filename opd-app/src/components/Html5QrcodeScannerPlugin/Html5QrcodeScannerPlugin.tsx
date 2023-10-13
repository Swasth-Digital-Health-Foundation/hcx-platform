// file = Html5QrcodePlugin.jsx
import { Html5QrcodeScanner } from "html5-qrcode";
import { useEffect, useState } from "react";

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

// const Html5QrcodePlugin = (props: any) => {
//   useEffect(() => {
//     // when component mounts
//     const config = createConfig(props);
//     const verbose = props.verbose === true;
//     // Suceess callback is required.
//     if (!props.qrCodeSuccessCallback) {
//       throw 'qrCodeSuccessCallback is required callback.';
//     }
//     const html5QrcodeScanner = new Html5QrcodeScanner(
//       qrcodeRegionId,
//       config,
//       verbose
//     );
//     html5QrcodeScanner.render(
//       props.qrCodeSuccessCallback,
//       props.qrCodeErrorCallback
//     );

//     return () => {
//       html5QrcodeScanner.clear().catch((error) => {
//         console.error('Failed to clear html5QrcodeScanner. ', error);
//       });
//     };
//   }, []);

//   return <div id={qrcodeRegionId} />;
// };

// export default Html5QrcodePlugin;

const Html5QrcodePlugin = (props: any) => {
  const [initialized, setInitialized] = useState(props.setInitialized);
  const [html5QrcodeScanner, setHtml5QrcodeScanner] = useState<any>(null);

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
    }
  };

  const stopScanner = () => {
    if (html5QrcodeScanner) {
      html5QrcodeScanner.clear().catch((error: any) => {
        console.error("Failed to clear html5QrcodeScanner. ", error);
      });
    }
  };

  return (
    <div>
      <div className="flex justify-center gap-6 text-center">
        <button onClick={startScanner}>Click here to start Scanning</button>
        {/* <button onClick={stopScanner}>Stop Scanner</button> */}
      </div>
        <div id={qrcodeRegionId}></div>
    </div>
  );
};

export default Html5QrcodePlugin;
