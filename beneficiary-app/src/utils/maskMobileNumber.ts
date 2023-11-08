export default function maskMobileNumber(mobileNumber: string): string {
    // Check if the mobile number has at least 6 digits
    if (mobileNumber.length >= 6) {
        const maskedPart = mobileNumber.slice(0, 6).replace(/\d/g, 'X');
        const visiblePart = mobileNumber.slice(6);
        return maskedPart + visiblePart;
    } else {
        return "";
    }
}

// Example usage:
// const mobileNumber = '1234567890'; // Replace with your mobile number
// const masked = maskMobileNumber(mobileNumber);
// console.log(masked);