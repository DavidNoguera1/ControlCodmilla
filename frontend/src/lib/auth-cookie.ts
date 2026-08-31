export const COOKIE_NAME = "cms_basic";

export function encodeBasic(username: string, password: string): string {
  return Buffer.from(`${username}:${password}`, "utf8").toString("base64");
}

export function parseBasic(token: string): { username: string; password: string } | null {
  try {
    const decoded = Buffer.from(token, "base64").toString("utf8");
    const idx = decoded.indexOf(":");
    if (idx <= 0) return null;
    return {
      username: decoded.slice(0, idx),
      password: decoded.slice(idx + 1),
    };
  } catch {
    return null;
  }
}
