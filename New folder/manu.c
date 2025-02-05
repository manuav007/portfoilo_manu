#include <stdio.h> 
#include <math.h>
int recur(int *x,int i,int sum,int y){    
    if(i==(y-1)) {sum+=x[i];return sum;    }
    else {   sum+=x[i];   return recur(x,++i,sum,y); }   
}
int main(){    int l,p,s,z,b,i;
    printf("enter the size of the array ");
    scanf("%d",&b);
    int a[b];
    for(i=0;i<b;i++){ scanf("%d",&a[i]);    }
    l=0;s=0; z=0;
    z= (sizeof(a) / sizeof(a[0]));
     printf("size    %d",z);  p =  recur(a,l,s,z);
    printf("  the sum is    %d",p); return 0;}
