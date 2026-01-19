export function CurrencyFormat(amount: number) {
    const currency_str = amount.toString();
    const result_parts = [];
    for(let i = currency_str.length; i > 0; i = i - 3) {
        const start_idx = (i - 3 > 0) ? (i - 3) : 0;
        result_parts.push(currency_str.substring(start_idx, i));
    }
    return result_parts.reverse().join('.') + '₫';
}