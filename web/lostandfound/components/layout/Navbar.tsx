"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { LogOut, ShieldAlert, Menu, X, Package, PlusCircle, LayoutDashboard, MessageCircle, Inbox, UserCog } from "lucide-react";
import { toast } from "sonner";

export function Navbar() {
    const [user, setUser] = useState<any>(null);
    const [mobileOpen, setMobileOpen] = useState(false);
    const pathname = usePathname();
    const router = useRouter();

    useEffect(() => {
        const fetchUser = async () => {
            const token = localStorage.getItem("token");
            if (!token || token === 'undefined' || token === 'null') return;

            try {
                const res = await fetch("http://localhost:8080/api/users/me", {
                    headers: { "Authorization": `Bearer ${token}` }
                });
                if (res.ok) {
                    const data = await res.json();
                    setUser(data);
                    if (data.role) localStorage.setItem("role", data.role);
                } else if (res.status === 401) {
                    localStorage.removeItem("token");
                }
            } catch (err) {
                console.error("Failed to fetch user in Navbar", err);
            }
        };

        fetchUser();
    }, [pathname]);

    useEffect(() => {
        setMobileOpen(false);
    }, [pathname]);

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        setUser(null);
        setMobileOpen(false);
        toast.success("Successfully logged out");
        router.push("/login");
    };

    const isActive = (href: string) => {
        if (href === "/") return pathname === "/";
        return pathname.startsWith(href);
    };

    const linkClass = (href: string) =>
        `transition-all duration-200 ${isActive(href)
            ? "text-rose-900 dark:text-rose-400 bg-rose-50 dark:bg-rose-900/30 font-semibold"
            : "hover:text-rose-800 hover:bg-rose-50 dark:hover:text-rose-400 dark:hover:bg-rose-900/30"
        }`;

    return (
        <nav className="border-b bg-white/80 dark:bg-slate-950/80 backdrop-blur-md sticky top-0 z-50 border-rose-100 dark:border-rose-900/50 shadow-sm">
            <div className="flex h-16 items-center px-4 md:px-6 container mx-auto">
                <Link href="/" className="text-xl font-extrabold tracking-tight flex-shrink-0">
                    <span className="text-rose-900 dark:text-rose-500">Lost</span>
                    <span className="text-amber-500 dark:text-amber-400">&</span>
                    <span className="text-rose-900 dark:text-rose-500">Found</span>
                </Link>

                <div className="ml-auto flex items-center gap-1">
                    <div className="hidden sm:flex items-center gap-1">
                        <Link href="/items">
                            <Button variant="ghost" size="sm" className={linkClass("/items")}>Catalog</Button>
                        </Link>
                        <Link href="/report">
                            <Button variant="ghost" size="sm" className={linkClass("/report")}>Report Item</Button>
                        </Link>
                        {user && (
                            <>
                                <Link href="/my-items">
                                    <Button variant="ghost" size="sm" className={linkClass("/my-items")}>My Items</Button>
                                </Link>
                                <Link href="/chat/global">
                                    <Button variant="ghost" size="sm" className={linkClass("/chat/global")}>Global Chat</Button>
                                </Link>
                                <Link href="/messages">
                                    <Button variant="ghost" size="sm" className={linkClass("/messages")}>Messages</Button>
                                </Link>
                                {user.role === "ADMIN" && (
                                    <Link href="/admin">
                                        <Button variant="ghost" size="sm" className={linkClass("/admin")}>Admin</Button>
                                    </Link>
                                )}
                            </>
                        )}
                    </div>

                    {user ? (
                        <div className="hidden sm:flex items-center gap-2 border-l border-slate-200 dark:border-slate-800 pl-3 ml-1">
                            <Link href="/profile" className="flex items-center gap-2 group">
                                <div className={`h-8 w-8 rounded-full p-0.5 shadow-sm group-hover:shadow-md transition-shadow ${isActive("/profile")
                                    ? "bg-gradient-to-tr from-amber-500 to-rose-500 ring-2 ring-amber-400/50"
                                    : "bg-gradient-to-tr from-rose-800 to-amber-500"
                                    }`}>
                                    <div className="h-full w-full rounded-full bg-white dark:bg-slate-900 flex items-center justify-center overflow-hidden">
                                        {user.avatarUrl ? (
                                            <img src={user.avatarUrl} alt="Avatar" className="object-cover w-full h-full" />
                                        ) : (
                                            <span className="text-xs font-bold text-rose-800 dark:text-rose-400 uppercase">{user.username?.[0]}</span>
                                        )}
                                    </div>
                                </div>
                                <div className="flex flex-col items-start gap-0.5 max-w-[120px]">
                                    <span className={`hidden md:inline-block text-sm font-semibold capitalize truncate w-full ${isActive("/profile") ? "text-rose-800" : "text-slate-700 dark:text-slate-300 group-hover:text-rose-800"}`} title={user.username}>{user.username}</span>
                                    {user.warningMarks > 0 && (
                                        <div className="hidden md:flex items-center gap-1 bg-amber-100/80 dark:bg-amber-900/40 text-amber-700 dark:text-amber-400 text-[10px] font-bold px-1.5 py-0.5 rounded-sm border border-amber-200 dark:border-amber-700/50">
                                            <ShieldAlert className="h-2.5 w-2.5" />
                                            <span>{user.warningMarks} Warning{user.warningMarks > 1 ? 's' : ''}</span>
                                        </div>
                                    )}
                                </div>
                            </Link>
                            <Button variant="ghost" size="icon" onClick={handleLogout} className="text-slate-500 hover:text-rose-700 hover:bg-rose-50" title="Logout">
                                <LogOut className="h-4 w-4" />
                            </Button>
                        </div>
                    ) : (
                        <div className="hidden sm:flex items-center gap-2 pl-2">
                            <Link href="/login">
                                <Button variant="ghost" size="sm" className={linkClass("/login")}>Sign In</Button>
                            </Link>
                            <Link href="/register">
                                <Button size="sm" className="bg-rose-900 text-white hover:bg-rose-950 shadow-sm">Sign Up</Button>
                            </Link>
                        </div>
                    )}

                    <Button
                        variant="ghost"
                        size="icon"
                        className="sm:hidden ml-1 text-slate-600 hover:text-rose-800 hover:bg-rose-50"
                        onClick={() => setMobileOpen(!mobileOpen)}
                        aria-label="Toggle menu"
                    >
                        {mobileOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
                    </Button>
                </div>
            </div>

            {mobileOpen && (
                <div className="sm:hidden border-t border-rose-100 dark:border-rose-900/40 bg-white/95 dark:bg-slate-950/95 backdrop-blur-md pb-4 px-4">
                    <div className="flex flex-col gap-1 pt-3">
                        <Link href="/items" onClick={() => setMobileOpen(false)}>
                            <Button variant="ghost" className={`w-full justify-start gap-2 ${linkClass("/items")}`}>
                                <Package className="h-4 w-4" /> Catalog
                            </Button>
                        </Link>
                        <Link href="/report" onClick={() => setMobileOpen(false)}>
                            <Button variant="ghost" className={`w-full justify-start gap-2 ${linkClass("/report")}`}>
                                <PlusCircle className="h-4 w-4" /> Report Item
                            </Button>
                        </Link>
                        {user && (
                            <>
                                <Link href="/my-items" onClick={() => setMobileOpen(false)}>
                                    <Button variant="ghost" className={`w-full justify-start gap-2 ${linkClass("/my-items")}`}>
                                        <LayoutDashboard className="h-4 w-4" /> My Items
                                    </Button>
                                </Link>
                                <Link href="/chat/global" onClick={() => setMobileOpen(false)}>
                                    <Button variant="ghost" className={`w-full justify-start gap-2 ${linkClass("/chat/global")}`}>
                                        <MessageCircle className="h-4 w-4" /> Global Chat
                                    </Button>
                                </Link>
                                <Link href="/messages" onClick={() => setMobileOpen(false)}>
                                    <Button variant="ghost" className={`w-full justify-start gap-2 ${linkClass("/messages")}`}>
                                        <Inbox className="h-4 w-4" /> Messages
                                    </Button>
                                </Link>
                                {user.role === "ADMIN" && (
                                    <Link href="/admin" onClick={() => setMobileOpen(false)}>
                                        <Button variant="ghost" className={`w-full justify-start gap-2 ${linkClass("/admin")}`}>
                                            <UserCog className="h-4 w-4" /> Admin
                                        </Button>
                                    </Link>
                                )}
                            </>
                        )}

                        <div className="border-t border-slate-100 dark:border-slate-800 mt-2 pt-2">
                            {user ? (
                                <div className="flex flex-col gap-1">
                                    <Link href="/profile" onClick={() => setMobileOpen(false)}>
                                        <div className="flex items-center gap-3 px-3 py-2 rounded-md hover:bg-rose-50 dark:hover:bg-rose-900/20 transition-colors cursor-pointer">
                                            <div className="h-9 w-9 rounded-full bg-gradient-to-tr from-rose-800 to-amber-500 p-0.5 flex-shrink-0">
                                                <div className="h-full w-full rounded-full bg-white dark:bg-slate-900 flex items-center justify-center overflow-hidden">
                                                    {user.avatarUrl ? (
                                                        <img src={user.avatarUrl} alt="Avatar" className="object-cover w-full h-full" />
                                                    ) : (
                                                        <span className="text-xs font-bold text-rose-800 dark:text-rose-400 uppercase">{user.username?.[0]}</span>
                                                    )}
                                                </div>
                                            </div>
                                            <div className="max-w-[180px]">
                                                <p className="text-sm font-semibold capitalize text-slate-800 dark:text-slate-200 truncate" title={user.username}>{user.username}</p>
                                                <p className="text-xs text-slate-500 truncate" title={user.email}>{user.email}</p>
                                            </div>
                                        </div>
                                    </Link>
                                    <Button
                                        variant="ghost"
                                        className="w-full justify-start gap-2 text-rose-700 hover:text-rose-800 hover:bg-rose-50"
                                        onClick={handleLogout}
                                    >
                                        <LogOut className="h-4 w-4" /> Sign Out
                                    </Button>
                                </div>
                            ) : (
                                <div className="flex flex-col gap-2">
                                    <Link href="/login" onClick={() => setMobileOpen(false)}>
                                        <Button variant="outline" className="w-full border-rose-200 text-rose-800">Sign In</Button>
                                    </Link>
                                    <Link href="/register" onClick={() => setMobileOpen(false)}>
                                        <Button className="w-full bg-rose-900 text-white hover:bg-rose-950">Sign Up</Button>
                                    </Link>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </nav>
    );
}
