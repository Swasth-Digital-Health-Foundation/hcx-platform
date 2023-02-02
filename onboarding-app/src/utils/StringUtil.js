

export function replaceString(str, numberOfChar, charToReplace) {
    return str.substring(0, numberOfChar).split("").map(ele => ele = charToReplace).join("").concat(str.substring(numberOfChar, str.length))
}