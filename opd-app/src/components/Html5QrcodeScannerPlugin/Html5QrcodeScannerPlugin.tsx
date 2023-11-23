import { Html5QrcodeScanner } from 'html5-qrcode';
import { useEffect } from 'react';

const qrcodeRegionId = "html5qr-code-full-region";

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
  useEffect(() => {
    const config = createConfig(props);
    const verbose = props.verbose === true;

    // Request camera permission
    navigator.mediaDevices
      .getUserMedia({ video: true })
      .then(() => {
        // Permission granted, proceed with scanner setup
        if (!(props.qrCodeSuccessCallback)) {
          throw "qrCodeSuccessCallback is a required callback.";
        }

        const html5QrcodeScanner = new Html5QrcodeScanner(
          qrcodeRegionId,
          config,
          verbose
        );

        html5QrcodeScanner.render(
          props.qrCodeSuccessCallback,
          props.qrCodeErrorCallback
        );

        // Cleanup function when component will unmount
        return () => {
          html5QrcodeScanner.clear().catch((error) => {
            console.error("Failed to clear html5QrcodeScanner. ", error);
          });
        };
      })
      .catch((error) => {
        // Handle permission denied or other errors
        console.error("Failed to access camera. ", error);
      });
  }, []);

  return (
    <>
      <div className='text-center'>
        Scan provider QR code to capture patient details
      </div>
      <div id={qrcodeRegionId} />
    </>
  );
};

export default Html5QrcodePlugin;
