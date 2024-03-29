import * as jose from 'jose'
import { v4 as uuidv4 } from 'uuid'

export async function generateJWS(issuer, subject) {

    const alg = 'RS256'
    // TODO: remove private key
    const pkcs8 = `-----BEGIN PRIVATE KEY-----
    MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCG+XLPYiCxrZq7
    1IX+w7uoDGxGI7qy7XaDbL3BJE33ju7rjdrP7wsAOWRvM8BIyWuRZZhl9xG+u7l/
    7OsZAzGoqI7p+32x+r9IJVzboLDajk6tp/NPg1csc7f2M5Bu6rkLEvrKLz3dgy3Q
    928rMsD3rSmzBLelfKTo+aDXvCOiw1dMWsZZdkEpCTJxH39Nb2K4S59kO/R2GtSU
    /QMLq65m34XcMZpDtatA1u1S8JdZNNeMCO+NuFKBzIfvXUCQ8jkf7h612+UP1AYh
    oyCMFpzUZ9b7liQF9TYpX1Myr/tT75WKuRlkFlcALUrtVskL8KA0w6sA0nX5fORV
    suVehVeDAgMBAAECggEAX1n1y5/M7PhxqWO3zYTFGzC7hMlU6XZsFOhLHRjio5Ks
    ImgyPlbm9J+W3iA3JLR2c17MTKxAMvg3UbIzW5YwDLAXViC+aW90like8mEQzzVd
    S7ysXG2ytcqCGUHQNStI0hP0a8T39XbodQl31ZKjU9VW8grRGe12Kse+4ukcW6yR
    VES+CkyO5BQB+vs3voZavodRGsk/YSt00PtIrFPJgkDuyzzcybKJD9zeJk5W3OGV
    K1z0on+NXKekRti5FBx/uEkT3+knkz7ZlTDNcyexyeiv7zSL/L6tcszV0Fe0g9vJ
    ktqnenEyh4BgbqABPzQR++DaCgW5zsFiQuD0hMadoQKBgQC+rekgpBHsPnbjQ2Pt
    og9cFzGY6LRGXxVcY7hKBtAZOKAKus5RmMi7Uv7aYJgtX2jt6QJMuE90JLEgdO2v
    xYG5V7H6Tx+HqH7ftCGZq70A9jFBaba04QAp0r4TnD6v/LM+PGVT8FKtggp+o7gZ
    qXYlSVFm6YzI37G08w43t2j2aQKBgQC1Nluxop8w6pmHxabaFXYomNckziBNMML5
    GjXW6b0xrzlnZo0p0lTuDtUy2xjaRWRYxb/1lu//LIrWqSGtzu+1mdmV2RbOd26P
    ArKw0pYpXhKFu/W7r6n64/iCisoMJGWSRJVK9X3D4AjPaWOtE+jUTBLOk0lqPJP8
    K6yiCA6ZCwKBgDLtgDaXm7HdfSN1/Fqbzj5qc3TDsmKZQrtKZw5eg3Y5CYXUHwbs
    J7DgmfD5m6uCsCPa+CJFl/MNWcGxeUpZFizKn16bg3BYMIrPMao5lGGNX9p4wbPN
    5J1HDD1wnc2jULxupSGmLm7pLKRmVeWEvWl4C6XQ+ykrlesef82hzwcBAoGBAKGY
    3v4y4jlSDCXaqadzWhJr8ffdZUrQwB46NGb5vADxnIRMHHh+G8TLL26RmcET/p93
    gW518oGg7BLvcpw3nOZaU4HgvQjT0qDvrAApW0V6oZPnAQUlarTU1Uk8kV9wma9t
    P6E/+K5TPCgSeJPg3FFtoZvcFq0JZoKLRACepL3vAoGAMAUHmNHvDI+v0eyQjQxl
    meAscuW0KVAQQR3OdwEwTwdFhp9Il7/mslN1DLBddhj6WtVKLXu85RIGY8I2NhMX
    LFMgl+q+mvKMFmcTLSJb5bJHyMz/foenGA/3Yl50h9dJRFItApGuEJo/30cG+VmY
    o2rjtEifktX4mDfbgLsNwsI=
    -----END PRIVATE KEY-----`
    
    const privateKey = await jose.importPKCS8(pkcs8, alg)

    const jwt = new jose.SignJWT({})
        .setProtectedHeader({ alg: 'RS256' })
        .setJti(uuidv4())
        .setIssuedAt()
        .setIssuer(issuer)
        .setSubject(subject)
        .setExpirationTime('2h')
        .sign(privateKey)

    return jwt;
}

