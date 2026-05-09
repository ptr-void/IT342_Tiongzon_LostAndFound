// Feature: Items — API calls

const BASE_URL = "http://localhost:8080/api";

export interface ItemDTO {
  id?: number;
  title: string;
  description?: string;
  status: "LOST" | "FOUND" | "RESOLVED";
  category: "ELECTRONICS" | "CLOTHING" | "DOCUMENTS" | "VALUABLES" | "OTHER";
  locationLat?: number;
  locationLng?: number;
  locationDescription?: string;
  imagePath?: string;
  reporterId?: number;
  reporterName?: string;
  reporterEmail?: string;
  reporterWarningMarks?: number;
  reporterAvatar?: string;
  createdAt?: string;
  lastUpdate?: string;
}

export async function getAllItems(token?: string): Promise<ItemDTO[]> {
  const headers: Record<string, string> = {};
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const response = await fetch(`${BASE_URL}/items`, { headers });
  if (!response.ok) throw new Error("Failed to fetch items");
  return response.json();
}

export async function getItemById(id: number, token?: string): Promise<ItemDTO> {
  const headers: Record<string, string> = {};
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const response = await fetch(`${BASE_URL}/items/${id}`, { headers });
  if (!response.ok) throw new Error("Item not found");
  return response.json();
}

export async function createItem(dto: ItemDTO, token: string): Promise<ItemDTO> {
  const response = await fetch(`${BASE_URL}/items`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(dto),
  });
  const json = await response.json().catch(() => ({ message: "Connection error" }));
  if (!response.ok) throw new Error(json.message || "Failed to create item");
  return json;
}

export async function updateItem(id: number, dto: Partial<ItemDTO>, token: string): Promise<ItemDTO> {
  const response = await fetch(`${BASE_URL}/items/${id}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(dto),
  });
  const json = await response.json().catch(() => ({ message: "Connection error" }));
  if (!response.ok) throw new Error(json.message || "Failed to update item");
  return json;
}

export async function deleteItem(id: number, token: string): Promise<void> {
  const response = await fetch(`${BASE_URL}/items/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) throw new Error("Failed to delete item");
}
