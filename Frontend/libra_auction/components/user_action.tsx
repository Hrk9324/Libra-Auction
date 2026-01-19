import { UserActionType } from "@/types/user_action_type";
export default function UserAction({items}: {items: UserActionType[]}) {
    return (
        <ul className="flex flex-1 gap-4 items-center justify-end">
            {items.map(item => {
                return <li key={item.value}><a href={item.href}>{item.value}</a></li>
            })}
        </ul>
    );
}