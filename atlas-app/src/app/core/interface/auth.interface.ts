export interface RegisterCommand {
  email: string,
  password: string,
  firstName: string,
  lastName: string
}

export interface LoginCommand {
  email: string,
  password: string
}

export interface TokenPair {
  accessToken: string,
  refreshToken: string
}

export interface RegisterResponse {
  message: string;
}

export interface MessageResponse {
  message: string;
}
