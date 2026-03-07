"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { useEffect, useState } from "react";
import { User, LogOut, ShieldAlert } from "lucide-react";
import { toast } from "sonner";

export function Navbar() {
    const [user, setUser] = useState<any>(null);
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

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        setUser(null);
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
                <Link href="/" className="text-xl font-extrabold tracking-tight">
                    <span className="text-rose-900 dark:text-rose-500">Lost</span>
                    <span className="text-amber-500 dark:text-amber-400">&</span>
                    <span className="text-rose-900 dark:text-rose-500">Found</span>
                </Link>

                <div className="ml-auto flex items-center space-x-1 sm:space-x-2">
                    {user ? (
                        <div className="flex items-center gap-4 border-l border-slate-200 dark:border-slate-800 pl-4 ml-2">
                            <Link href="/" className="flex items-center gap-2 group">
                                <div className={`h-8 w-8 rounded-full p-0.5 shadow-sm group-hover:shadow-md transition-shadow bg-gradient-to-tr from-rose-800 to-amber-500`}>
                                    <div className="h-full w-full rounded-full bg-white dark:bg-slate-900 flex items-center justify-center overflow-hidden">
                                        {user.avatarUrl ? (
                                            <img src={user.avatarUrl} alt="Avatar" className="object-cover w-full h-full" />
                                        ) : (
                                            <span className="text-xs font-bold text-rose-800 dark:text-rose-400 uppercase">{user.username?.[0]}</span>
                                        )}
                                    </div>
                                </div>
                                <div className="flex flex-col items-start gap-0.5">
                                    <span className="hidden md:inline-block text-sm font-semibold capitalize text-slate-700 dark:text-slate-300 group-hover:text-rose-800">{user.username}</span>
                                    {user.warningMarks > 0 && (
                                        <div className="hidden md:flex items-center gap-1 bg-amber-100/80 dark:bg-amber-900/40 text-amber-700 dark:text-amber-400 text-[10px] font-bold px-1.5 py-0.5 rounded-sm border border-amber-200 dark:border-amber-700/50" title={`${user.warningMarks} Warning(s)`}>
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
                        <div className="flex items-center gap-2 pl-2">
                            <Link href="/login">
                                <Button variant="ghost" className={linkClass("/login")}>Sign In</Button>
                            </Link>
                            <Link href="/register">
                                <Button className="bg-rose-900 text-white hover:bg-rose-950 shadow-sm hidden sm:inline-flex">Sign Up</Button>
                            </Link>
                        </div>
                    )}
                </div>
            </div>
        </nav>
    );
}
