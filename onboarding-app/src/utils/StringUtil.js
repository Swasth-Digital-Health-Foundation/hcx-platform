

export function replaceString(str, numberOfChar, charToReplace) {
    return str.substring(0, numberOfChar).split("").map(ele => ele = charToReplace).join("").concat(str.substring(numberOfChar, str.length))
}

export function maskEmailAddress(emailAddress) {
    if(emailAddress) {
        var id = emailAddress.substring(0, emailAddress.lastIndexOf("@"));
        var domain = emailAddress.substring(emailAddress.lastIndexOf("@"));
        if (id.length <= 1) {
            return emailAddress;
        }
        switch (id.length) {
            case 2:
                id = id.substring(0, 1) + "*";
                break;
            case 3:
                id = id.substring(0, 1) + "*" + id.substring(2);
                break;
            case 4:
                id = id.substring(0, 1) + "**" + id.substring(3);
                break;
            default:
                var masks = Array(id.length - 4 + 1).join("*");
                id = id.substring(0, 2) + masks + id.substring(id.length - 2);
                break;
        }
    
        domain = maskDomain(domain);
    
        var address = id + domain;
        return address;
    }

}

export function maskDomain(domain) {
    const domainLength = domain.length;
    if (domainLength > 2) {
        domain = domain.substring(0, 3);
        for (var i = 0; i < domainLength; i++) {
            domain = domain + "*";
        }
    }
    return domain;
}

