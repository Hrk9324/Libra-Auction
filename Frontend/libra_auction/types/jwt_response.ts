export interface JWTResponse {
    token: string,
    refreshToken: string,
    type: string,
    accessTokenExpiration: number,
    refreshTokenExpiration: number
}