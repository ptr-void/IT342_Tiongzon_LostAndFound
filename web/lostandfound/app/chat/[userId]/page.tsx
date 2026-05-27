"use client";

import { Suspense, useEffect, useRef, useState } from "react";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { ArrowLeft, Loader2, Send } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api";

function PrivateChatContent() {
  const params = useParams();
  const searchParams = useSearchParams();
  const router = useRouter();
  const otherUserId = params.userId as string;
  const itemId = searchParams.get("itemId");
  const clientRef = useRef<Client | null>(null);
  const endRef = useRef<HTMLDivElement>(null);
  const [user, setUser] = useState<any>(null);
  const [otherUser, setOtherUser] = useState<any>(null);
  const [item, setItem] = useState<any>(null);
  const [messages, setMessages] = useState<any[]>([]);
  const [input, setInput] = useState("");
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => { endRef.current?.scrollIntoView({ behavior: "smooth" }); }, [messages]);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) return router.push("/login");
    if (!itemId) { setLoading(false); return; }
    const headers = { Authorization: `Bearer ${token}` };

    const init = async () => {
      const meRes = await fetch(`${API_BASE}/users/me`, { headers });
      if (!meRes.ok) return router.push("/login");
      const me = await meRes.json();
      setUser(me);
      if (me.userId?.toString() === otherUserId) { setLoading(false); return; }
      const [otherRes, itemRes, historyRes] = await Promise.all([
        fetch(`${API_BASE}/users/${otherUserId}`, { headers }),
        fetch(`${API_BASE}/items/${itemId}`),
        fetch(`${API_BASE}/messages/private/${otherUserId}?itemId=${itemId}`, { headers }),
      ]);
      if (otherRes.ok) setOtherUser(await otherRes.json());
      if (itemRes.ok) setItem(await itemRes.json());
      if (historyRes.ok) setMessages(await historyRes.json());

      const socket = new SockJS(`${API_BASE}/ws`);
      const client = new Client({
        webSocketFactory: () => socket as any,
        connectHeaders: headers,
        onConnect: () => {
          setConnected(true);
          setLoading(false);
          client.subscribe("/user/queue/messages", (message) => {
            const next = JSON.parse(message.body);
            const rightUser = next.senderId?.toString() === otherUserId || next.receiverId?.toString() === otherUserId;
            const rightItem = next.itemId?.toString() === itemId;
            if (rightUser && rightItem) setMessages((prev) => next.id && prev.some((msg) => msg.id === next.id) ? prev : [...prev, next]);
          });
        },
        onWebSocketClose: () => setConnected(false),
      });
      client.activate();
      clientRef.current = client;
    };

    init();
    return () => { clientRef.current?.deactivate(); };
  }, [itemId, otherUserId, router]);

  const sendMessage = (event: React.FormEvent) => {
    event.preventDefault();
    if (!input.trim() || !connected || !itemId || !clientRef.current) return;
    clientRef.current.publish({ destination: "/app/chat.private", body: JSON.stringify({ senderId: user.userId, receiverId: Number(otherUserId), itemId: Number(itemId), content: input.trim() }) });
    setInput("");
  };

  if (!itemId) return <div className="flex h-[calc(100vh-4rem)] flex-col items-center justify-center gap-3"><p>No item specified.</p><Link href="/items"><Button variant="outline">Browse Items</Button></Link></div>;
  if (loading) return <div className="flex h-[calc(100vh-4rem)] items-center justify-center"><Loader2 className="h-10 w-10 animate-spin text-rose-800" /></div>;
  if (user?.userId?.toString() === otherUserId) return <div className="flex h-[calc(100vh-4rem)] flex-col items-center justify-center gap-3"><p>You cannot message yourself about your own item.</p></div>;

  return <div className="mx-auto flex h-[calc(100vh-4rem)] max-w-4xl flex-col px-4 py-6"><Link href="/messages" className="mb-3 inline-flex items-center text-sm text-rose-800"><ArrowLeft className="mr-2 h-4 w-4" /> Back to Messages</Link><Card className="flex min-h-0 flex-1 flex-col"><CardHeader className="border-b"><CardTitle className="flex items-center gap-3"><span className="flex h-9 w-9 items-center justify-center overflow-hidden rounded-full bg-gradient-to-tr from-rose-800 to-amber-500 text-sm font-bold text-white">{otherUser?.avatarUrl ? <img src={otherUser.avatarUrl} alt={otherUser.username} className="h-full w-full object-cover" /> : (otherUser?.username || "U")[0]?.toUpperCase()}</span><span>Chat with {otherUser?.username || "User"} <span className="text-sm font-normal text-slate-500">about {item?.title}</span></span></CardTitle></CardHeader><CardContent className="min-h-0 flex-1 space-y-4 overflow-y-auto p-4">{messages.map((msg, index) => { const isMe = msg.senderId === user?.userId; return <div key={msg.id || index} className={`flex ${isMe ? "justify-end" : "justify-start"}`}><div className={`max-w-[75%] rounded-2xl px-4 py-2 text-sm ${isMe ? "bg-amber-600 text-white" : "bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-100"}`}>{msg.content}</div></div>; })}<div ref={endRef} /></CardContent><CardFooter className="border-t p-4"><form onSubmit={sendMessage} className="flex w-full gap-2"><input value={input} onChange={(e) => setInput(e.target.value)} disabled={!connected} placeholder="Type your message..." className="h-10 flex-1 rounded-md border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-amber-500" /><Button type="submit" disabled={!connected || !input.trim()}><Send className="h-4 w-4" /> Send</Button></form></CardFooter></Card></div>;
}

export default function PrivateChatPage() {
  return <Suspense fallback={<div className="flex h-[calc(100vh-4rem)] items-center justify-center"><Loader2 className="h-10 w-10 animate-spin text-rose-800" /></div>}><PrivateChatContent /></Suspense>;
}
