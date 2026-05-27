"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { Loader2, MessageCircle, Send } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api";

export default function GlobalChatPage() {
  const router = useRouter();
  const clientRef = useRef<Client | null>(null);
  const endRef = useRef<HTMLDivElement>(null);
  const [user, setUser] = useState<any>(null);
  const [messages, setMessages] = useState<any[]>([]);
  const [input, setInput] = useState("");
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => { endRef.current?.scrollIntoView({ behavior: "smooth" }); }, [messages]);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) return router.push("/login");
    const headers = { Authorization: `Bearer ${token}` };

    const init = async () => {
      try {
        const meRes = await fetch(`${API_BASE}/users/me`, { headers });
        if (!meRes.ok) throw new Error("Not logged in");
        setUser(await meRes.json());
        const historyRes = await fetch(`${API_BASE}/messages/global`, { headers });
        if (historyRes.ok) setMessages(await historyRes.json());

        const socket = new SockJS(`${API_BASE}/ws`);
        const client = new Client({
          webSocketFactory: () => socket as any,
          connectHeaders: headers,
          onConnect: () => {
            setConnected(true);
            setLoading(false);
            client.subscribe("/topic/global", (message) => {
              const next = JSON.parse(message.body);
              setMessages((prev) => next.id && prev.some((msg) => msg.id === next.id) ? prev : [...prev, next]);
            });
          },
          onWebSocketClose: () => setConnected(false),
          onStompError: () => toast.error("Chat server connection failed."),
        });
        client.activate();
        clientRef.current = client;
      } catch {
        router.push("/login");
      }
    };

    init();
    return () => { clientRef.current?.deactivate(); };
  }, [router]);

  const sendMessage = (event: React.FormEvent) => {
    event.preventDefault();
    if (!input.trim() || !connected || !clientRef.current) return;
    clientRef.current.publish({ destination: "/app/chat.global", body: JSON.stringify({ senderId: user.userId, content: input.trim() }) });
    setInput("");
  };

  if (loading) return <div className="flex h-[calc(100vh-4rem)] items-center justify-center"><Loader2 className="h-10 w-10 animate-spin text-rose-800" /></div>;

  return <div className="mx-auto flex h-[calc(100vh-4rem)] max-w-4xl flex-col px-4 py-6"><Card className="flex min-h-0 flex-1 flex-col"><CardHeader className="border-b"><CardTitle className="flex items-center gap-3"><MessageCircle className="h-6 w-6 text-rose-800" /> Global Community Chat <span className={`h-2.5 w-2.5 rounded-full ${connected ? "bg-emerald-500" : "bg-red-500"}`} /></CardTitle></CardHeader><CardContent className="min-h-0 flex-1 space-y-4 overflow-y-auto p-4">{messages.map((msg, index) => { const isMe = msg.senderId === user?.userId; const initial = (msg.senderName || "U")[0]?.toUpperCase(); return <div key={msg.id || index} className={`flex items-end gap-2 ${isMe ? "justify-end" : "justify-start"}`}>{!isMe && <div className="flex h-8 w-8 shrink-0 items-center justify-center overflow-hidden rounded-full bg-gradient-to-tr from-rose-800 to-amber-500 text-xs font-bold text-white">{msg.senderAvatar ? <img src={msg.senderAvatar} alt={msg.senderName} className="h-full w-full object-cover" /> : initial}</div>}<div className={`max-w-[75%] rounded-2xl px-4 py-2 text-sm ${isMe ? "bg-rose-800 text-white" : "bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-100"}`}><div className="mb-1 text-xs opacity-75">{isMe ? "You" : msg.senderName}</div>{msg.content}</div></div>; })}<div ref={endRef} /></CardContent><CardFooter className="border-t p-4"><form onSubmit={sendMessage} className="flex w-full gap-2"><input value={input} onChange={(e) => setInput(e.target.value)} disabled={!connected} placeholder="Type your message..." className="h-10 flex-1 rounded-md border bg-background px-3 text-sm outline-none focus:ring-2 focus:ring-rose-500" /><Button type="submit" disabled={!connected || !input.trim()}><Send className="h-4 w-4" /> Send</Button></form></CardFooter></Card></div>;
}
