// Feature: Auth — API calls

const BASE_URL = "http://localhost:8080/api";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export async function loginUser(data: LoginRequest): Promise<{ token: string }> {
  const response = await fetch(`${BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });

  const json = await response.json().catch(() => ({ message: "Connection error" }));
  if (!response.ok) {
    throw new Error(json.message || "Failed to log in.");
  }
  return json;
}

export async function registerUser(data: RegisterRequest): Promise<{ message: string }> {
  const response = await fetch(`${BASE_URL}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });

  const json = await response.json().catch(() => ({ message: "Connection error" }));
  if (!response.ok) {
    throw new Error(json.message || "Registration failed.");
  }
  return json;
}
