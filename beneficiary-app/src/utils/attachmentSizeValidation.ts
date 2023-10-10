export const handleFileChange: any = (
  event: any,
  setErrorMsg: any,
  setIsSuccess: any,
  setSelectedFile: any
) => {
  const files = event.target.files;
  const MAX_FILE_SIZE = 1 * 1024 * 1024;

  const validFiles = Array.from(files).filter(
    (file: any) => file.size <= MAX_FILE_SIZE
  );
  const hasInvalidFileSize = validFiles.length !== files.length;

  setSelectedFile((prevSelectedFiles: any) => {
    const updatedFiles = hasInvalidFileSize
      ? [] // Clear the files if there are invalid ones
      : [...(prevSelectedFiles || []), ...validFiles]; // Use the previous files if available

    return updatedFiles;
  });
  setErrorMsg(
    hasInvalidFileSize
      ? 'File size is greater than the maximum limit (1MB)'
      : ''
  );
  setIsSuccess(!hasInvalidFileSize);
};
