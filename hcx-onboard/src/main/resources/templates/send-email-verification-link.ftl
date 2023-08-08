<html>
<body>
Dear ${USER_NAME},<br/><br/>
Thank you for initiating the HCX onboarding process.<br/><br/>
Your Participant code is : <b>${PARTICIPANT_CODE}</b><#if USER_ID != ""> and Root Admin User Id is : <b>${USER_ID}</b></#if>.<br/><br/>
Please follow the steps below to complete the email verification process:<br/><ol><li>
 Click on the <a href= ${URL}>Verification link</a> and open it in your preferred web browser.</li><br/>
<li> Click on the "Verify Email" button provided on the verification page.</li></ol>
Verification link once generated is valid for ${DAY} days, visit participant profile page for resending a new link.<br/><br/>
<#if role == "payor">
   <b>Note:</b> For payors, to complete the identity verification, Please reach out to HCX Team.
   <br/><br/>
</#if>
Thank you for choosing HCX as your preferred data exchange platform. If you have any questions or need assistance, please sign up at <a href="https://swasth.zohodesk.in/portal/en/signin">swasth-zohodesk</a> and raise a ticket to contact our support team.<br/><br/>
<i>If you did not create an account with HCX, please ignore this email. We apologise for any inconvenience caused.</i><br/><br/>
Best regards,<br/>
HCX onboarding team
</body>
</html>
