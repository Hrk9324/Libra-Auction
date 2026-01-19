import { NavType } from "@/types/nav_type";
export default function Nav({items}: {items: NavType[]}) {
    return(
        <nav className="flex flex-2">
            <ul className="flex flex-1 gap-12 justify-center items-center font-semibold">
                {items.map(item => (
                    <li key={item.value}><a href={item.href}>{item.value}</a></li>
                ))}
            </ul>
        </nav>
    );
}