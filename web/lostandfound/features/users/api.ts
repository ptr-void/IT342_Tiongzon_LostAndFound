

const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api";

export interface UserProfile {
  userId: number;
  username: string;
  email: string;
  role: string;
  avatarUrl?: string;
  warningMarks: number;
  active: boolean;
  banned: boolean;
  createdAt: string;
  lastUpdate: string;
}

export async function getMyProfile(token: string): Promise<UserProfile> {
  const response = await fetch(`${BASE_URL}/users/me`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) throw new Error("Failed to fetch user");
  return response.json();
}

export async function updateMyAvatar(token: string, avatarUrl: string): Promise<{ message: string; avatarUrl: string }> {
  const response = await fetch(`${BASE_URL}/users/me/avatar`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ avatarUrl }),
  });
  const json = await response.json().catch(() => ({ message: "Connection error" }));
  if (!response.ok) throw new Error(json.message || "Failed to update avatar");
  return json;
}
