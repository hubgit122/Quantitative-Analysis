package ssq.utils.security;

public enum ConnectionStatus
{
    CONNECTING, /*等待服务器发送一个被我的根证书签名的证书*/
    ASK_REGISTERED,
    NAME_AND_R_REG,
    USE_CERT_REG,
    REG_CERT,
    REG_PASS,
    USE_CERT_LOG,
    NAME_AND_R_PASS,
    NAME_AND_R_CERT,
    CHECK_PASS,
    CHECK_R,
    LOGGED_IN,
    LOGGED_OUT; /**/
}
