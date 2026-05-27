"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { ArrowRight, Inbox, Loader2, MessageCircle, Package } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api";

export default function MessagesPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);
  const [conversations, setConversations] = useState<any[]>([]);

  useEffect(() => {
    const load = async () => {
      const token = localStorage.getItem("token");
      if (!token) return router.push("/login");
      const headers = { Authorization: `Bearer ${token}` };
      try {
        const meRes = await fetch(`${API_BASE}/users/me`, { headers });
        if (!meRes.ok) throw new Error("Not logged in");
        const me = await meRes.json();
        setCurrentUserId(me.userId);
        const convRes = await fetch(`${API_BASE}/messages/conversations`, { headers });
        if (convRes.ok) setConversations(await convRes.json());
      } catch {
        router.push("/login");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [router]);

  if (loading) return <div className="flex h-[calc(100vh-4rem)] items-center justify-center"><Loader2 className="h-10 w-10 animate-spin text-rose-800" /></div>;

  return <div className="min-h-[calc(100vh-4rem)] bg-slate-50 px-4 py-8 dark:bg-slate-950"><div className="container mx-auto max-w-3xl"><div className="mb-6 flex items-center gap-3"><Inbox className="h-8 w-8 text-amber-600" /><div><h1 className="text-2xl font-extrabold">Messages</h1><p className="text-sm text-slate-500">Your private conversations about items.</p></div></div>{conversations.length === 0 ? <Card><CardContent className="flex flex-col items-center py-16 text-center"><MessageCircle className="mb-4 h-10 w-10 text-slate-400" /><h2 className="font-semibold">No messages yet</h2><p className="mb-4 text-sm text-slate-500">Start from an item detail page.</p><Link href="/items"><Button variant="outline">Browse Items</Button></Link></CardContent></Card> : <div className="space-y-3">{conversations.map((conv, index) => <Link key={`${conv.userId}-${conv.itemId}-${index}`} href={`/chat/${conv.userId}?itemId=${conv.itemId}`}><Card className="transition hover:border-amber-300 hover:shadow-md"><CardContent className="flex items-center gap-4 p-4"><div className="flex h-11 w-11 items-center justify-center rounded-full bg-gradient-to-tr from-rose-800 to-amber-500 font-bold uppercase text-white">{conv.avatarUrl ? <img src={conv.avatarUrl} alt={conv.username} className="h-full w-full rounded-full object-cover" /> : conv.username?.[0]}</div><div className="min-w-0 flex-1"><div className="flex justify-between gap-3"><span className="font-bold">{conv.username}</span><span className="text-xs text-slate-400">{new Date(conv.lastMessageAt).toLocaleDateString()}</span></div><div className="flex items-center gap-1 text-xs text-slate-500"><Package className="h-3 w-3" /> <span className="truncate">{conv.itemTitle}</span></div><p className="truncate text-sm text-slate-600">{conv.lastMessageSenderId === currentUserId ? "You: " : ""}{conv.lastMessage}</p></div><ArrowRight className="h-4 w-4 text-slate-400" /></CardContent></Card></Link>)}</div>}</div></div>;
}
