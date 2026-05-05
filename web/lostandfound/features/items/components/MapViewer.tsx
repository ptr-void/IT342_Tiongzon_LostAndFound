"use client";

import { useEffect, useRef, useState } from "react";
import L from "leaflet";

const ICON = L.icon({
    iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
    shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
    iconSize: [25, 41],
    iconAnchor: [12, 41],
});

interface MapViewerProps {
    lat: number;
    lng: number;
}

export default function MapViewer({ lat, lng }: MapViewerProps) {
    const containerRef = useRef<HTMLDivElement>(null);
    const mapRef = useRef<L.Map | null>(null);
    const [ready, setReady] = useState(false);

    useEffect(() => {
        setReady(true);
    }, []);

    useEffect(() => {
        if (!ready || !containerRef.current) return;
        if (mapRef.current) return;

        const map = L.map(containerRef.current, {
            center: [lat, lng],
            zoom: 15,
            scrollWheelZoom: false,
            zoomControl: true,
        });

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        }).addTo(map);

        L.marker([lat, lng], { icon: ICON }).addTo(map);

        mapRef.current = map;

        return () => {
            map.remove();
            mapRef.current = null;
        };
    }, [ready]);

    return (
        <div className="h-full w-full relative">
            <div ref={containerRef} className="h-full w-full rounded-md z-0 border border-slate-200" />
            {!ready && (
                <div className="absolute inset-0 bg-slate-100 dark:bg-slate-800 rounded-md animate-pulse" />
            )}
        </div>
    );
}
