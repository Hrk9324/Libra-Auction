import { NextResponse } from 'next/server'
import { NextRequest } from 'next/server'
import { isAuthenticated } from './lib/is_authenticated';
import { getRole } from './lib/get_roles';

export async function proxy(request: NextRequest) {
  const pathname = request.nextUrl.pathname;
  const isBffRoute = pathname.startsWith('/bff/');
  if (isBffRoute) {
    return NextResponse.next();
  }
  
  if (pathname == '/sign-in' || pathname == '/sign-up') {
    if (await isAuthenticated()) {
      return NextResponse.redirect(new URL('/', request.url))
    }
  }

  const isPersonalRoute = pathname.startsWith('/profile') || pathname.startsWith('/seller-dashboard');
  const isAdminRoute = pathname.startsWith('/admin-dashboard');
  
  if (isPersonalRoute || isAdminRoute) {
    const role = await getRole();

    if (!role) {
      return NextResponse.redirect(new URL('/sign-in', request.url));
    }

    if (isAdminRoute && role.name !== 'ADMIN') {
      return NextResponse.redirect(new URL('/', request.url));
    }

    if (isPersonalRoute && role.name !== 'USER' && role.name !== 'ADMIN') {
      return NextResponse.redirect(new URL('/', request.url));
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: '/((?!bff|_next/static|_next_image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
}