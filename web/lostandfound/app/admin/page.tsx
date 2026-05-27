"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { FileCheck, Loader2, ShieldAlert, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { StatusBadge } from "@/components/ui/status-badge";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api";

export default function AdminDashboard() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [users, setUsers] = useState<any[]>([]);
  const [items, setItems] = useState<any[]>([]);
  const [claims, setClaims] = useState<any[]>([]);

  useEffect(() => {
    const load = async () => {
      const token = localStorage.getItem("token");
      if (!token) return router.push("/login");
      const headers = { Authorization: `Bearer ${token}` };
      try {
        const meRes = await fetch(`${API_BASE}/users/me`, { headers });
        const me = meRes.ok ? await meRes.json() : null;
        if (!me || me.role !== "ADMIN") {
          toast.error("Admin access required.");
          return router.push("/");
        }
        const [usersRes, itemsRes, claimsRes] = await Promise.all([
          fetch(`${API_BASE}/admin/users`, { headers }),
          fetch(`${API_BASE}/admin/items`, { headers }),
          fetch(`${API_BASE}/admin/claims`, { headers }),
        ]);
        if (usersRes.ok) setUsers(await usersRes.json());
        if (itemsRes.ok) setItems(await itemsRes.json());
        if (claimsRes.ok) setClaims(await claimsRes.json());
      } catch {
        toast.error("Failed to load admin dashboard.");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [router]);

  const postAdminAction = async (url: string) => {
    const token = localStorage.getItem("token");
    const res = await fetch(url, { method: "POST", headers: { Authorization: `Bearer ${token}` } });
    if (!res.ok) throw new Error((await res.json().catch(() => ({}))).message || "Action failed");
    return res.json();
  };

  const warnUser = async (userId: number) => {
    try {
      const data = await postAdminAction(`${API_BASE}/admin/users/${userId}/warn`);
      setUsers((prev) => prev.map((u) => (u.userId === userId ? { ...u, warningMarks: data.warningMarks } : u)));
      toast.success("User warned.");
    } catch (e: any) {
      toast.error(e.message);
    }
  };

  const toggleBan = async (userId: number) => {
    try {
      const data = await postAdminAction(`${API_BASE}/admin/users/${userId}/ban`);
      setUsers((prev) => prev.map((u) => (u.userId === userId ? { ...u, banned: data.isBanned } : u)));
      toast.success(data.message);
    } catch (e: any) {
      toast.error(e.message);
    }
  };

  const deleteItem = async (itemId: number) => {
    if (!confirm("Delete this item post?")) return;
    const token = localStorage.getItem("token");
    const res = await fetch(`${API_BASE}/admin/items/${itemId}`, { method: "DELETE", headers: { Authorization: `Bearer ${token}` } });
    if (res.ok) {
      setItems((prev) => prev.filter((item) => item.id !== itemId));
      toast.success("Post deleted.");
    } else {
      toast.error("Failed to delete post.");
    }
  };


  if (loading) return <div className="flex h-[calc(100vh-4rem)] items-center justify-center"><Loader2 className="h-10 w-10 animate-spin text-rose-800" /></div>;

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-slate-50 px-4 py-8 dark:bg-slate-950">
      <div className="container mx-auto max-w-6xl">
        <div className="mb-8 flex items-center gap-3">
          <ShieldAlert className="h-9 w-9 text-rose-800" />
          <div><h1 className="text-3xl font-extrabold">Admin Dashboard</h1><p className="text-slate-500">Manage users, warnings, bans, posts, and claims. Private messages are excluded.</p></div>
        </div>
        <Tabs defaultValue="users">
          <TabsList className="mb-6 grid w-full max-w-lg grid-cols-3"><TabsTrigger value="users">Users</TabsTrigger><TabsTrigger value="posts">Posts</TabsTrigger><TabsTrigger value="claims">Claims</TabsTrigger></TabsList>
          <TabsContent value="users">
            <Card><CardHeader><CardTitle>User Directory</CardTitle><CardDescription>Moderate registered accounts.</CardDescription></CardHeader><CardContent className="overflow-x-auto">
              <table className="w-full text-sm"><thead><tr className="border-b text-left"><th className="p-3">User</th><th className="p-3">Role</th><th className="p-3">Status</th><th className="p-3">Warnings</th><th className="p-3 text-right">Actions</th></tr></thead><tbody>
                {users.map((user) => <tr key={user.userId} className="border-b"><td className="p-3"><div className="font-semibold">{user.username}</div><div className="text-xs text-slate-500">{user.email}</div></td><td className="p-3"><Badge variant="outline">{user.role}</Badge></td><td className="p-3"><Badge variant={user.banned ? "destructive" : "outline"}>{user.banned ? "Banned" : "Active"}</Badge></td><td className="p-3">{user.warningMarks}</td><td className="space-x-2 p-3 text-right"><Button size="sm" variant="outline" disabled={user.role === "ADMIN"} onClick={() => warnUser(user.userId)}>Warn</Button><Button size="sm" variant={user.banned ? "secondary" : "destructive"} disabled={user.role === "ADMIN"} onClick={() => toggleBan(user.userId)}>{user.banned ? "Unban" : "Ban"}</Button></td></tr>)}
              </tbody></table>
            </CardContent></Card>
          </TabsContent>
          <TabsContent value="posts">
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {items.map((item) => <Card key={item.id}><CardHeader><div className="flex justify-between gap-3"><CardTitle className="line-clamp-1">{item.title}</CardTitle><StatusBadge status={item.status} /></div><CardDescription>{item.category} by {item.reporterName}</CardDescription></CardHeader><CardContent className="space-y-4"><p className="line-clamp-3 text-sm text-slate-600">{item.description}</p><div className="flex gap-2"><Link href={`/items/${item.id}`} className="flex-1"><Button variant="outline" className="w-full">View</Button></Link><Button variant="destructive" onClick={() => deleteItem(item.id)}><Trash2 className="h-4 w-4" /> Delete</Button></div></CardContent></Card>)}
              {items.length === 0 && <Card className="md:col-span-2 lg:col-span-3"><CardContent className="py-10 text-center text-slate-500">No posts available to moderate.</CardContent></Card>}
            </div>
          </TabsContent>
          <TabsContent value="claims">
            <Card><CardHeader><CardTitle className="flex items-center gap-2"><FileCheck className="h-5 w-5 text-rose-800" /> Claims Overview</CardTitle><CardDescription>View all claim submissions and payment status. Private conversations are not shown.</CardDescription></CardHeader><CardContent className="overflow-x-auto">
              <table className="w-full text-sm"><thead><tr className="border-b text-left"><th className="p-3">Item</th><th className="p-3">Claimant</th><th className="p-3">Status</th><th className="p-3">Payment</th><th className="p-3">Proof</th><th className="p-3 text-right">Actions</th></tr></thead><tbody>
                {claims.map((claim) => <tr key={claim.id} className="border-b"><td className="p-3"><div className="font-semibold">{claim.itemTitle || `Item #${claim.itemId}`}</div><div className="text-xs text-slate-500">Claim #{claim.id}</div></td><td className="p-3">{claim.claimantName || "Unknown"}</td><td className="p-3"><StatusBadge status={claim.status} /></td><td className="p-3"><StatusBadge status={claim.paymentStatus || "NOT_APPLICABLE"} /></td><td className="max-w-xs p-3"><p className="line-clamp-2 text-slate-600">{claim.proofDescription || "No proof description"}</p>{claim.proofImagePath && <a href={claim.proofImagePath} target="_blank" className="mt-1 block text-xs text-rose-800 underline">View proof image</a>}</td><td className="p-3 text-right"><Link href={`/claims/${claim.id}`} className="text-sm font-medium text-rose-800 hover:underline">View Details</Link></td></tr>)}
                {claims.length === 0 && <tr><td colSpan={6} className="p-8 text-center text-slate-500">No claims submitted.</td></tr>}
              </tbody></table>
            </CardContent></Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
