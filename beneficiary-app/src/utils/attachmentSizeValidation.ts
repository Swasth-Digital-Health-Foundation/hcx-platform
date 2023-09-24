export const handleFileChange: any = (
  event: any,
  setErrorMsg: any,
  setIsSuccess: any,
  setSelectedFile: any,
) => {
  const files = event.target.files;
  const MAX_FILE_SIZE = 4 * 1024 * 1024;

  const validFiles = Array.from(files).filter(
    (file: any) => file.size <= MAX_FILE_SIZE
  );
  const hasInvalidFileSize = validFiles.length !== files.length;

  setSelectedFile(hasInvalidFileSize ? [] : validFiles);
  setErrorMsg(
    hasInvalidFileSize
      ? 'File size is greater than the maximum limit (4MB)'
      : ''
  );
  setIsSuccess(!hasInvalidFileSize);

  // if (!hasInvalidFileSize) {
  //   const base64Promises = validFiles.map((file: any) => {
  //     return new Promise((resolve) => {
  //       const reader = new FileReader();
  //       reader.onload = (e: any) => {
  //         resolve({
  //           name: file.name,
  //           type: file.type,
  //           size: file.size,
  //           base64: e.target.result.split(',')[1], // Extract Base64 data
  //         });
  //       };
  //       reader.readAsDataURL(file);
  //     });
  //   });

  //   // Use Promise.all to wait for all files to be processed
  //   Promise.all(base64Promises).then((base64Files) => {
  //     setSelectedFile(base64Files);
  //   });
  // }
};
